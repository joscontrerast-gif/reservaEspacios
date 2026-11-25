const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const { DatabaseSync } = require('node:sqlite');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(express.json());

// Base de datos SQLite independiente para Auth
const db = new DatabaseSync('./auth.db');

db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    profile TEXT NOT NULL
  )
`);

function hashPassword(password, salt) {
  return crypto.pbkdf2Sync(password, salt, 10000, 32, 'sha256').toString('hex');
}

function createUser(name, email, password, profile) {
  const salt = crypto.randomBytes(16).toString('hex');
  const hash = hashPassword(password, salt);
  const stmt = db.prepare(
    'INSERT INTO users (name, email, password_hash, salt, profile) VALUES (?, ?, ?, ?, ?)'
  );
  return stmt.run(name.trim(), email.trim(), hash, salt, profile);
}

// Datos iniciales (Seed) idénticos a la app Android
const countUsers = db.prepare('SELECT COUNT(*) as total FROM users').get().total;
if (countUsers === 0) {
  createUser('Coordinador de espacios', 'admin@instituto.cl', 'Admin1234', 'ADMIN');
  createUser('Docente de prueba', 'docente@instituto.cl', 'Docente123', 'TEACHER');
  createUser('Estudiante Demo', 'estudiante@instituto.cl', 'Estudiante123', 'STUDENT');
  createUser('Ana Pérez', 'ana@instituto.cl', 'Admin1234', 'STAFF');
}

// Health check
app.get('/health', (_req, res) => res.json({ service: 'auth-service', status: 'OK' }));

// 1. Login
app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: 'Correo y contraseña requeridos' });
  }

  const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email.trim());
  if (!user) {
    return res.status(401).json({ message: 'Correo o contraseña incorrectos' });
  }

  const hash = hashPassword(password, user.salt);
  if (hash !== user.password_hash) {
    return res.status(401).json({ message: 'Correo o contraseña incorrectos' });
  }

  res.json({
    id: user.id,
    name: user.name,
    email: user.email,
    password: '',
    profile: user.profile
  });
});

// 2. Registro
app.post('/api/auth/register', (req, res) => {
  const { name, email, password, profile } = req.body;
  if (!name || !email || !password || !profile) {
    return res.status(400).json({ message: 'Todos los campos son obligatorios' });
  }

  const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(email.trim());
  if (existing) {
    return res.status(409).json({ message: 'Este correo ya está registrado' });
  }

  const result = createUser(name, email, password, profile);
  res.status(201).json({
    id: Number(result.lastInsertRowid),
    name: name.trim(),
    email: email.trim(),
    password: '',
    profile
  });
});

// 3. Recuperación de contraseña (requisito rúbrica)
app.post('/api/auth/recover-password', (req, res) => {
  const { email, newPassword } = req.body;
  if (!email || !newPassword) {
    return res.status(400).json({ message: 'Correo y nueva contraseña requeridos' });
  }

  const user = db.prepare('SELECT id FROM users WHERE email = ?').get(email.trim());
  if (!user) {
    return res.status(404).json({ message: 'No existe una cuenta asociada a ese correo' });
  }

  const salt = crypto.randomBytes(16).toString('hex');
  const hash = hashPassword(newPassword, salt);
  db.prepare('UPDATE users SET password_hash = ?, salt = ? WHERE id = ?').run(hash, salt, user.id);

  res.json({ message: 'Contraseña actualizada correctamente' });
});

// 4. Modificación de perfil (requisito rúbrica)
app.put('/api/auth/users/:id', (req, res) => {
  const id = Number(req.params.id);
  const { name, password } = req.body;

  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(id);
  if (!user) {
    return res.status(404).json({ message: 'Usuario no encontrado' });
  }

  const updatedName = name && name.trim() ? name.trim() : user.name;
  if (password && password.trim()) {
    const salt = crypto.randomBytes(16).toString('hex');
    const hash = hashPassword(password, salt);
    db.prepare('UPDATE users SET name = ?, password_hash = ?, salt = ? WHERE id = ?')
      .run(updatedName, hash, salt, id);
  } else {
    db.prepare('UPDATE users SET name = ? WHERE id = ?').run(updatedName, id);
  }

  res.json({
    id: user.id,
    name: updatedName,
    email: user.email,
    password: '',
    profile: user.profile
  });
});

// 5. Buscar por ID
app.get('/api/auth/users/:id', (req, res) => {
  const user = db.prepare('SELECT id, name, email, profile FROM users WHERE id = ?')
    .get(Number(req.params.id));
  if (!user) {
    return res.status(404).json({ message: 'Usuario no encontrado' });
  }
  res.json({ ...user, password: '' });
});

app.listen(PORT, () => {
  console.log(`[auth-service] corriendo en http://localhost:${PORT}`);
});