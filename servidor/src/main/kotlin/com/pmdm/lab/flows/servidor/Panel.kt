package com.pmdm.lab.flows.servidor

/** El panel de control: se abre en el navegador y sirve para «portarse mal» con el compañero. */
internal fun panelHtml(): String = """
<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<title>Mercado · Panel de control</title>
<style>
  body { font-family: system-ui, sans-serif; max-width: 640px; margin: 2rem auto; padding: 0 1rem; }
  h1 { font-size: 1.4rem; }
  label { display: block; margin-top: 1rem; font-weight: 600; }
  input { width: 100%; padding: .4rem; box-sizing: border-box; }
  small { color: #555; font-weight: 400; }
  button { margin-top: 1.2rem; padding: .5rem 1.2rem; }
  pre { background: #f2f2f2; padding: 1rem; border-radius: 6px; }
</style>
</head>
<body>
<h1>Panel de control del mercado</h1>

<h2>Caos</h2>
<form id="caos">
  <label>Latencia (ms) <small>lo que tarda cada petición</small>
    <input name="latenciaMs" type="number" value="300"></label>
  <label>Fallos (%) <small>probabilidad de que una petición falle con un 503</small>
    <input name="fallo" type="number" value="0" min="0" max="100"></label>
  <label>Siempre fallan <small>separadas por comas: catalogo, detalle, perfil, logros, amigos</small>
    <input name="siempreFallan" type="text" placeholder="logros"></label>
  <button>Aplicar</button>
</form>

<h2>Lo que ha hecho tu pareja</h2>
<pre id="estadisticas">…</pre>

<script>
  document.getElementById('caos').addEventListener('submit', async e => {
    e.preventDefault();
    await fetch('/admin/caos', { method: 'POST', body: new URLSearchParams(new FormData(e.target)) });
  });
  async function refrescar() {
    try {
      const s = await (await fetch('/estadisticas')).json();
      document.getElementById('estadisticas').textContent =
        'Peticiones:                ' + s.peticiones + '\n' +
        'Conexiones a precios:      ' + s.conexionesPrecios + '\n' +
        'Conexiones a ranking:      ' + s.conexionesRanking;
    } catch (e) {}
  }
  setInterval(refrescar, 1000); refrescar();
</script>
</body>
</html>
""".trimIndent()
