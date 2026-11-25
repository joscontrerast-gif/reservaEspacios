const express = require('express');
const cors = require('cors');
const { DatabaseSync } = require('node:sqlite');

const app = express();
const PORT = process.env.PORT || 3003;
const SPACES_SERVICE_URL = process.env.SPACES_SERVICE_URL || 'http://localhost:3002';

app.use(cors());
app.use(express.json());

// Base de datos SQLite independiente para Reservas
const db = new DatabaseSync('./reservations.db');

db.exec(`
  CREATE TABLE IF NOT EXISTS reservations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    space_id INTEGER NOT NULL,
    space_name TEXT NOT NULL,
    requester_id INTEGER NOT NULL,
    requester_name TEXT NOT NULL,
    activity TEXT NOT NULL,
    attendees INTEGER NOT NULL,
    date TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    status TEXT NOT NULL,
    rejection_reason TEXT
  )
`);

// Seed inicial
const countRes = db.prepare('SELECT COUNT(*) as total FROM reservations').get().total;
if (countRes === 0) {
  const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
  const insertRes = db.prepare(`
    INSERT INTO reservations (space_id, space_name, requester_id, requester_name, activity, attendees, date, start_time, end_time, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
  `);
  insertRes.run(1, 'Teatro', 2, 'Docente de prueba', 'Ensayo de obra', 30, tomorrow, '10:00', '12:00');
  insertRes.run(1, 'Teatro', 4, 'Ana Pérez (Administrativo)', 'Charla de bienvenida', 80, tomorrow, '11:00', '13:00');
}

function mapReservation(r) {
  return {
    id: r.id,
    spaceId: r.space_id,
    spaceName: r.space_name,
    requesterId: r.requester_id,
    requesterName: r.requester_name,
    activity: r.activity,
    attendees: r.attendees,
    date: r.date,
    startTime: r.start_time,
    endTime: r.end_time,
    status: r.status,
    rejectionReason: r.rejection_reason || null
  };
}

app.get('/health', (_req, res) => res.json({ service: 'reservations-service', status: 'OK' }));

// 1. Listar todas las reservas
app.get('/api/reservations', (_req, res) => {
  const rows = db.prepare('SELECT * FROM reservations ORDER BY id').all();
  res.json(rows.map(mapReservation));
});

// 2. Crear nueva solicitud de reserva
app.post('/api/reservations', (req, res) => {
  const { spaceId, spaceName, requesterId, requesterName, activity, attendees, date, startTime, endTime } = req.body;

  const info = db.prepare(`
    INSERT INTO reservations (space_id, space_name, requester_id, requester_name, activity, attendees, date, start_time, end_time, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
  `).run(
    Number(spaceId), spaceName, Number(requesterId), requesterName,
    activity, Number(attendees), date, startTime, endTime
  );

  const created = db.prepare('SELECT * FROM reservations WHERE id = ?').get(Number(info.lastInsertRowid));
  res.status(201).json(mapReservation(created));
});

// 3. Aprobar solicitud (con comunicación entre microservicios para prioridad académica)
app.put('/api/reservations/:id/approve', async (req, res) => {
  const id = Number(req.params.id);
  const target = db.prepare('SELECT * FROM reservations WHERE id = ?').get(id);

  if (!target) return res.status(404).json({ message: 'La solicitud ya no existe' });
  if (target.status !== 'PENDING') return res.status(400).json({ message: 'Esta solicitud ya fue resuelta' });

  // Comunicación entre microservicios: consultar clases académicas a spaces-service
  try {
    const resp = await fetch(`${SPACES_SERVICE_URL}/api/schedules`);
    if (resp.ok) {
      const classes = await resp.json();
      const dateObj = new Date(`${target.date}T00:00:00`);
      const isoDay = dateObj.getDay() === 0 ? 7 : dateObj.getDay();

      const classConflict = classes.find(
        (c) =>
          c.spaceId === target.space_id &&
          c.dayOfWeek === isoDay &&
          target.start_time < c.endTime &&
          c.startTime < target.end_time
      );
      if (classConflict) {
        return res.status(409).json({
          message: `No se puede aprobar: choca con la clase ${classConflict.subject} (${classConflict.startTime}-${classConflict.endTime})`
        });
      }
    }
  } catch (_err) {
    // Si spaces-service no responde localmente, continúa con validación de reservas
  }

  // Verificar choque con otras reservas ya aprobadas
  const conflict = db.prepare(`
    SELECT * FROM reservations
    WHERE id != ? AND status = 'APPROVED' AND space_id = ? AND date = ?
      AND ? < end_time AND start_time < ?
  `).get(id, target.space_id, target.date, target.start_time, target.end_time);

  if (conflict) {
    return res.status(409).json({
      message: `No se puede aprobar: ${conflict.space_name} ya está reservado de ${conflict.start_time} a ${conflict.end_time}`
    });
  }

  db.prepare("UPDATE reservations SET status = 'APPROVED', rejection_reason = NULL WHERE id = ?").run(id);
  res.json({ message: 'Reserva aprobada' });
});

// 4. Rechazar solicitud
app.put('/api/reservations/:id/reject', (req, res) => {
  const id = Number(req.params.id);
  const { reason = '' } = req.body;
  const target = db.prepare('SELECT * FROM reservations WHERE id = ?').get(id);

  if (!target) return res.status(404).json({ message: 'La solicitud ya no existe' });
  if (target.status !== 'PENDING') return res.status(400).json({ message: 'Esta solicitud ya fue resuelta' });

  db.prepare("UPDATE reservations SET status = 'REJECTED', rejection_reason = ? WHERE id = ?")
    .run(reason.trim(), id);
  res.json({ message: 'Reserva rechazada' });
});

// 5. Cancelar solicitud o reserva
app.put('/api/reservations/:id/cancel', (req, res) => {
  const id = Number(req.params.id);
  const target = db.prepare('SELECT * FROM reservations WHERE id = ?').get(id);

  if (!target) return res.status(404).json({ message: 'La solicitud ya no existe' });
  if (target.status !== 'PENDING' && target.status !== 'APPROVED') {
    return res.status(400).json({ message: 'Esta solicitud ya no se puede cancelar' });
  }

  db.prepare("UPDATE reservations SET status = 'CANCELLED' WHERE id = ?").run(id);
  res.json({ message: 'Reserva cancelada' });
});

app.listen(PORT, () => {
  console.log(`[reservations-service] corriendo en http://localhost:${PORT}`);
});