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
      window.location.href = tiqrUri;
    }
  }
});
