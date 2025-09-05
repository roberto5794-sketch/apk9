const Native = (function(){
  const has = typeof window.Android !== 'undefined';
  const st = document.getElementById('status');
  const set = (t, cls='muted') => { st.textContent=t; st.className=cls; };
  return {
    available: has,
    setStatus: set,
    ocr: async ()=>{
      set('Abrindo câmera...');
      if (has && window.Android.scanText) { window.Android.scanText(); }
      else set('Sem ponte Android (teste no app)', 'muted');
    },
    sttStart: async ()=>{
      if (has && window.Android.startListening){ set('Escutando (offline)...'); window.Android.startListening(); return; }
      set('STT disponível apenas no app nativo', 'muted');
    },
    sttStop: ()=>{
      if (has && window.Android.stopListening){ window.Android.stopListening(); }
      Native.setStatus('Parado');
    },
    onNativeResult: (type, payload)=>{
      if (type==='ocr'){ document.getElementById('raw').value = payload||''; set('OCR concluído'); }
      if (type==='stt'){
        const raw = document.getElementById('raw');
        raw.value = (raw.value + ' ' + (payload||'')).trim();
        set('Voz capturada');
      }
      if (type==='error'){ set(payload||'Erro', 'err'); }
    }
  };
})();
window.onNativeResult = Native.onNativeResult;
document.getElementById('btn-ocr').onclick = Native.ocr;
document.getElementById('btn-stt').onclick = Native.sttStart;
document.getElementById('btn-stop').onclick = Native.sttStop;
document.getElementById('btn-extract').onclick = ()=>{
  const s = document.getElementById('paste').value;
  const nums = (s.match(/[-+]?\d*\.?\d+/g) || []).map(Number);
  document.getElementById('extract-out').textContent = nums.join(', ');
};
document.getElementById('btn-computar').onclick = ()=>{
  const e0 = parseFloat(document.getElementById('e0').value);
  const n0 = parseFloat(document.getElementById('n0').value);
  const az = parseFloat(document.getElementById('az').value);
  const d  = parseFloat(document.getElementById('dist').value);
  const out = document.getElementById('out');
  if ([e0,n0,az,d].some(x=>Number.isNaN(x))) { out.value='Preencha E0, N0, Azimute e Distância.'; return; }
  const rad = (az*Math.PI)/180;
  const dE = d*Math.sin(rad);
  const dN = d*Math.cos(rad);
  const e1 = e0 + dE;
  const n1 = n0 + dN;
  document.getElementById('e1').value = e1.toFixed(3);
  document.getElementById('n1').value = n1.toFixed(3);
  out.value = `ΔE=${dE.toFixed(3)} m, ΔN=${dN.toFixed(3)} m\nE1=${e1.toFixed(3)}  N1=${n1.toFixed(3)}`;
};
document.getElementById('btn-clear').onclick = ()=>{
  ['e0','n0','az','dist','e1','n1','out','raw','paste','extract-out'].forEach(id=>{
    const el = document.getElementById(id);
    if (!el) return;
    if (el.tagName==='INPUT' || el.tagName==='TEXTAREA') el.value='';
    else el.textContent='';
  });
  Native.setStatus('pronto');
};
