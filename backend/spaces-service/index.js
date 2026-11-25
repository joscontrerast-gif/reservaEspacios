const express = require('express');
const cors = require('cors');
const { DatabaseSync } = require('node:sqlite');

const app = express();
const PORT = process.env.PORT || 3002;

app.use(cors());
app.use(express.json());

// Base de datos SQLite independiente para Espacios y Horarios
const db = new DatabaseSync('./spaces.db');
db.exec('PRAGMA foreign_keys = ON');

db.exec(`
  CREATE TABLE IF NOT EXISTS spaces (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    location TEXT NOT NULL DEFAULT '',
    photo_path TEXT
  );

  CREATE TABLE IF NOT EXISTS equipment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    space_id INTEGER NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    status TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS academic_classes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject TEXT NOT NULL,
    teacher TEXT NOT NULL,
    space_id INTEGER NOT NULL,
    space_name TEXT NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    students INTEGER NOT NULL
  );
`);

// Seed inicial
const countSpaces = db.prepare('SELECT COUNT(*) as total FROM spaces').get().total;
if (countSpaces === 0) {
  const insertSpace = db.prepare(
    'INSERT INTO spaces (code, name, capacity, location) VALUES (?, ?, ?, ?)'
  );
  const teatroId = Number(insertSpace.run('TEA-01', 'Teatro', 120, 'Edificio principal').lastInsertRowid);
  insertSpace.run('EST-01', 'Estudio de grabación', 15, 'Piso 2');
  insertSpace.run('TAL-01', 'Taller de diseño', 30, 'Pabellón B');
  insertSpace.run('SAL-01', 'Sala común', 40, 'Piso 1');

  const insertEq = db.prepare('INSERT INTO equipment (space_id, name, status) VALUES (?, ?, ?)');
  insertEq.run(teatroId, 'Proyector', 'AVAILABLE');
  insertEq.run(teatroId, 'Micrófonos', 'AVAILABLE');

  const insertClass = db.prepare(`
    INSERT INTO academic_classes (subject, teacher, space_id, space_name, day_of_week, start_time, end_time, students)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `);
  insertClass.run('Diseño Gráfico I', 'Prof. Rojas', 3, 'Taller de diseño', 1, '08:30', '10:00', 25);
  insertClass.run('Producción Audiovisual', 'Prof. Soto', 2, 'Estudio de grabación', 3, '14:00', '16:00', 12);
}

app.get('/health', (_req, res) => res.json({ service: 'spaces-service', status: 'OK' }));

// --- ENDPOINTS DE ESPACIOS ---

app.get('/api/spaces', (_req, res) => {
  const spaces = db.prepare('SELECT * FROM spaces ORDER BY id').all();
  const equipmentRows = db.prepare('SELECT space_id, name, status FROM equipment ORDER BY id').all();

  const mapped = spaces.map((s) => ({
    id: s.id,
    name: s.name,
    capacity: s.capacity,
    code: s.code,
    location: s.location,
    photoPath: s.photo_path || null,
    equipment: equipmentRows
      .filter((e) => e.space_id === s.id)
      .map((e) => ({ name: e.name, status: e.status }))
  }));

  res.json(mapped);
});

app.post('/api/spaces', (req, res) => {
  const { code, name, capacity, location = '', equipment = [], photoPath = null } = req.body;
  if (!code || !name || !capacity) {
    return res.status(400).json({ message: 'Código, nombre y capacidad son obligatorios' });
  }

  const normalizedCode = code.trim().toUpperCase();
  const exists = db.prepare('SELECT id FROM spaces WHERE UPPER(code) = ?').get(normalizedCode);
  if (exists) {
    return res.status(409).json({ message: 'Ya existe un espacio con ese código' });
  }

  const info = db.prepare(
    'INSERT INTO spaces (code, name, capacity, location, photo_path) VALUES (?, ?, ?, ?, ?)'
  ).run(normalizedCode, name.trim(), Number(capacity), location.trim(), photoPath);

  const newId = Number(info.lastInsertRowid);
  const insertEq = db.prepare('INSERT INTO equipment (space_id, name, status) VALUES (?, ?, ?)');
  equipment.forEach((eq) => {
    insertEq.run(newId, eq.name, eq.status || 'AVAILABLE');
  });

  res.status(201).json({
    id: newId,
    code: normalizedCode,
    name: name.trim(),
    capacity: Number(capacity),
    location: location.trim(),
    equipment,
    photoPath
  });
});

// --- ENDPOINTS DE HORARIOS ACADÉMICOS ---

app.get('/api/schedules', (_req, res) => {
  const rows = db.prepare('SELECT * FROM academic_classes ORDER BY id').all();
  const mapped = rows.map((r) => ({
    id: r.id,
    subject: r.subject,
    teacher: r.teacher,
    spaceId: r.space_id,
    spaceName: r.space_name,
    dayOfWeek: r.day_of_week,
    startTime: r.start_time,
    endTime: r.end_time,
    students: r.students
  }));
  res.json(mapped);
});

app.post('/api/schedules', (req, res) => {
  const { spaceId, spaceName, dayOfWeek, subject, teacher, startTime, endTime, students } = req.body;

  // Validar solapamiento de clases
  const sameDayClasses = db.prepare(
    'SELECT * FROM academic_classes WHERE space_id = ? AND day_of_week = ?'
  ).all(Number(spaceId), Number(dayOfWeek));

  const overlap = sameDayClasses.find(
    (c) => startTime < c.end_time && c.start_time < endTime
  );
  if (overlap) {
    return res.status(409).json({
      message: `${spaceName} ya tiene la clase ${overlap.subject} (${overlap.start_time}-${overlap.end_time}) en ese horario`
    });
  }

  const info = db.prepare(`
    INSERT INTO academic_classes (subject, teacher, space_id, space_name, day_of_week, start_time, end_time, students)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `).run(subject, teacher, Number(spaceId), spaceName, Number(dayOfWeek), startTime, endTime, Number(students));

  res.status(201).json({
    id: Number(info.lastInsertRowid),
    subject,
    teacher,
    spaceId: Number(spaceId),
    spaceName,
    dayOfWeek: Number(dayOfWeek),
    startTime,
    endTime,
    students: Number(students)
  });
});

app.delete('/api/schedules/:id', (req, res) => {
  db.prepare('DELETE FROM academic_classes WHERE id = ?').run(Number(req.params.id));
  res.status(204).send();
});

app.listen(PORT, () => {
  console.log(`[spaces-service] corriendo en http://localhost:${PORT}`);
});