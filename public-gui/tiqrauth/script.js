document.addEventListener("DOMContentLoaded", function () {
  if (window.location.pathname === '/tiqrauth/' || window.location.pathname === '/tiqrauth') {
    const params = new URLSearchParams(window.location.search);

    const u = params.get('u');
    const s = params.get('s');
    const q = params.get('q');
    const i = params.get('i');
    const v = params.get('v');

    if (u && s && q && i && v) {
      const tiqrUri = `eduidauth://${u}@${i}/${s}/${q}/${i}/${v}`;

      document.body.innerHTML = `<p>Opening eduID app...</p>`;

      window.location.href = tiqrUri;
    } else {
      document.body.innerHTML = '<p>Misscing parameters.</p>';
    }
  } else {
    document.body.innerHTML = '<p>Incorrect path.</p>';
  }
});
