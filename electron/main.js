const { app, BrowserWindow, Menu, shell, globalShortcut } = require('electron');
const path = require('path');

function createWindow() {
  Menu.setApplicationMenu(null);
  const win = new BrowserWindow({
    width: 480,
    height: 860,
    minWidth: 360,
    minHeight: 560,
    backgroundColor: '#000000',
    title: 'Coverly',
    icon: path.join(__dirname, '..', 'assets', 'icon.png'),
    autoHideMenuBar: true,
    webPreferences: {
      autoplayPolicy: 'no-user-gesture-required',
      contextIsolation: true
    }
  });
  win.loadFile(path.join(__dirname, '..', 'www', 'index.html'));
  win.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });
  // Ctrl+Shift+T: окно поверх всех окон (мини-режим)
  win.webContents.on('before-input-event', (e, input) => {
    if (input.type === 'keyDown' && input.control && input.shift && String(input.key).toLowerCase() === 't') {
      win.setAlwaysOnTop(!win.isAlwaysOnTop());
      e.preventDefault();
    }
  });
}

app.whenReady().then(() => {
  createWindow();
  const run = (js) => {
    const w = BrowserWindow.getAllWindows()[0];
    if (w) w.webContents.executeJavaScript(js).catch(() => {});
  };
  // медиаклавиши клавиатуры
  globalShortcut.register('MediaPlayPause', () => run('toggle()'));
  globalShortcut.register('MediaNextTrack', () => run('step(1)'));
  globalShortcut.register('MediaPreviousTrack', () => run('step(-1)'));
});
app.on('will-quit', () => globalShortcut.unregisterAll());
app.on('window-all-closed', () => app.quit());
