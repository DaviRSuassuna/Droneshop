/* Seção "O que você vai voar?" no estilo Desenho: 5 variações, tema escuro da opção D */
(function(){
  let uid=0;
  const ART={
    film:()=>`
      <path class="soft" d="M20 138 C70 112 100 128 150 108 S240 88 280 100"/>
      <path class="soft" d="M20 152 C80 136 125 150 180 134 S250 128 280 136"/>
      <path d="M20 166 C90 156 145 170 200 156 S260 152 280 158"/>
      <circle class="soft" cx="206" cy="66" r="18"/><path class="soft" d="M206 40v-8M206 100v-8M180 66h-8M240 66h-8"/>
      <path d="M44 28H28v16M256 28h16v16M44 176H28v-16M256 176h16v-16"/>
      <path class="soft" d="M100 28v148M200 28v148M28 77h244M28 127h244" stroke-dasharray="1 4"/>
      <path class="hot draw" d="M64 140 C96 104 142 86 196 72"/>
      <circle class="dot" cx="196" cy="72" r="3.2"/>
      <circle class="rec" cx="40" cy="20" r="2.6"/><text x="47" y="23">REC 00:01:24</text><text x="272" y="23" text-anchor="end">4K 60 · 10-bit</text>
      <text x="32" y="192">ISO 100 · 1/120</text><text x="268" y="192" text-anchor="end">BAT 87%</text>`,
    insp:()=>`
      <path d="M110 186 138 22h24l28 164"/>
      <path d="M138 22h24M134 46h32M128 82h44M121 122h58M114 162h72"/>
      <path class="soft" d="M134 46l38 36M166 46l-38 36M128 82l51 40M172 82l-51 40M121 122l65 40M179 122l-65 40"/>
      <path d="M118 46h-20M182 46h20M96 46v6M204 46v6"/>
      <circle class="soft" cx="222" cy="98" r="12"/><circle class="soft" cx="222" cy="98" r="24"/><circle class="soft" cx="222" cy="98" r="38"/>
      <circle class="hot draw" cx="222" cy="98" r="5"/><path class="hot" d="M222 78v9M222 109v9M202 98h9M233 98h9"/>
      <rect class="tag" x="236" y="62" width="50" height="16" rx="3"/><text x="261" y="73" text-anchor="middle" class="ink">87,4 °C</text>
      <text x="20" y="22">TÉRMICA · IRONBOW</text>
      <rect class="soft" x="20" y="150" width="6" height="36"/><text x="32" y="156">92°</text><text x="32" y="188">18°</text>`,
    map:()=>`
      <g class="soft"><path d="M30 24v152M70 24v152M110 24v152M150 24v152M190 24v152M230 24v152M270 24v152M30 24h240M30 62h240M30 100h240M30 138h240M30 176h240"/></g>
      <path class="hot draw" d="M48 40H252V70H48V100H252V130H48V160H252"/>
      <circle class="dot" cx="48" cy="40" r="3.2"/><circle class="ring" cx="252" cy="160" r="4"/>
      <path class="soft" d="M262 40v30M258 40h8M258 70h8"/><text x="270" y="58" class="sm">75%</text>
      <text x="30" y="194">GSD 1,2 cm/px</text><text x="270" y="194" text-anchor="end">120 m AGL</text><text x="30" y="16">ROTA 03 · 2,1 km²</text>`,
    agro:()=>{const id='cl'+(++uid);return `
      <defs><clipPath id="${id}"><path d="M62 52 160 30 246 78 222 172 82 160Z"/></clipPath></defs>
      <g class="soft" clip-path="url(#${id})"><path d="M20 190 150 0M36 190 166 0M52 190 182 0M68 190 198 0M84 190 214 0M100 190 230 0M116 190 246 0M132 190 262 0M148 190 278 0M164 190 294 0M180 190 310 0M196 190 326 0M212 190 342 0"/></g>
      <path class="hot draw" d="M62 52 160 30 246 78 222 172 82 160Z"/>
      <path class="soft" d="M96 120 C130 104 168 108 206 92" stroke-dasharray="3 3"/>
      <circle class="dot" cx="156" cy="104" r="3.2"/>
      <rect class="tag" x="166" y="112" width="96" height="30" rx="3"/><text x="173" y="124" class="ink">TALHÃO 07</text><text x="173" y="136">48 ha · NDVI 0,72</text>
      <text x="20" y="22">MAPA DE VIGOR · 14 OUT</text>`}
  };
  const U=[
    {k:'film',t:'Filmagem e viagem',d:'Gimbal de 3 eixos e voos automáticos para filmar sem esforço.',n:'4K 60',u:'vídeo estabilizado',p:'a partir de <b>R$ 4.290</b>',m:'31 modelos',
     s:[['Vídeo','4K 60 fps'],['Peso','a partir de 249 g'],['Autonomia','até 43 min']]},
    {k:'insp',t:'Inspeção',d:'Térmica e zoom para achar o defeito antes de subir na torre.',n:'56×',u:'zoom · térmica 640×512',p:'a partir de <b>R$ 18.900</b>',m:'12 modelos',
     s:[['Térmica','640×512'],['Zoom','56× híbrido'],['Proteção','IP55']]},
    {k:'map',t:'Mapeamento',d:'Rotas automáticas com RTK para ortomosaico e topografia.',n:'1 cm',u:'precisão RTK',p:'a partir de <b>R$ 21.500</b>',m:'9 modelos',
     s:[['Posição','RTK 1 cm'],['GSD','1,2 cm/px'],['Área por voo','2,1 km²']]},
    {k:'agro',t:'Agricultura',d:'Pulverização e mapa de vigor por talhão, com suporte no campo.',n:'21 ha',u:'por hora · tanque de 40 L',p:'<b>sob cotação</b>',m:'7 modelos',
     s:[['Tanque','até 40 L'],['Rendimento','21 ha/h'],['Mapa','NDVI por talhão']]}
  ];
  const svg=(k,cls='')=>`<svg class="art ${cls}" viewBox="0 0 300 200" preserveAspectRatio="xMidYMid meet" aria-hidden="true">${ART[k]()}</svg>`;

  const V=[
    {n:'A · Ajustado',f:()=>`<div class="dA">${U.map(x=>`<a class="dcard" href="drones.html"><div class="stage">${svg(x.k)}</div><div class="body"><h3>${x.t}</h3><p>${x.d}</p><div class="meta"><span>${x.p}</span><span class="arrow">${x.m}</span></div></div></a>`).join('')}</div>`},
    {n:'B · Grande 2×2',f:()=>`<div class="dB">${U.map(x=>`<a class="dcard wide" href="#"><div class="stage">${svg(x.k)}</div><div class="body"><span class="lbl">${x.u}</span><h3>${x.t}</h3><p>${x.d}</p><ul>${x.s.map(s=>`<li>${s[0]}<b>${s[1]}</b></li>`).join('')}</ul><div class="meta"><span>${x.p}</span><span class="arrow">${x.m}</span></div></div></a>`).join('')}</div>`},
    {n:'C · Em lista',f:()=>`<div class="dC">${U.map(x=>`<a class="drow" href="#"><div class="stage">${svg(x.k)}</div><div class="txt"><h3>${x.t}</h3><p>${x.d}</p></div><div class="num">${x.n}<small>${x.u}</small></div><div class="end"><span>${x.p}</span><span class="arrow">${x.m}</span></div></a>`).join('')}</div>`},
    {n:'D · Planta com abas',f:()=>`<div class="dD"><div class="dtabs" role="tablist">${U.map((x,i)=>`<button type="button" role="tab" data-i="${i}" aria-selected="${i===0}"><span>${x.t}</span><small>${x.m}</small></button>`).join('')}</div><div class="dpane"></div></div>`},
    {n:'E · Desenho + número',f:()=>`<div class="dE">${U.map(x=>`<a class="dcard tall" href="#"><div class="stage">${svg(x.k)}<div class="over"><b>${x.n}</b><span>${x.u}</span></div></div><div class="body"><h3>${x.t}</h3><p>${x.d}</p><div class="meta"><span>${x.p}</span><span class="arrow">${x.m}</span></div></div></a>`).join('')}</div>`}
  ];
  function pane(el,i){const x=U[i];el.innerHTML=`<div class="stage big">${svg(x.k)}</div><div class="side"><span class="lbl">${x.u}</span><h3>${x.t}</h3><p>${x.d}</p><div class="kn">${x.n}</div><ul>${x.s.map(s=>`<li>${s[0]}<b>${s[1]}</b></li>`).join('')}</ul><div class="meta"><span>${x.p}</span><a class="cta" href="#">Ver ${x.m}</a></div></div>`}

  const css=`
  .dsw{display:flex;flex-wrap:wrap;align-items:center;gap:6px;margin:-6px 0 26px;padding:8px;border:1px dashed var(--line2);border-radius:12px}
  .dsw .lbl{margin:0 8px 0 6px}
  .dsw button{border:1px solid var(--line2);background:none;border-radius:999px;padding:7px 14px;font:500 13px var(--body);cursor:pointer;color:var(--ink2)}
  .dsw button[aria-pressed="true"]{background:var(--ink);border-color:var(--ink);color:var(--bg)}
  .dv[hidden]{display:none!important}
  .art{width:100%;height:100%;display:block}
  .art *{fill:none;stroke:#C9CDD3;stroke-width:1;vector-effect:non-scaling-stroke}
  .art .soft{stroke:#474D55}
  .art .hot{stroke:var(--amber);stroke-width:1.6}
  .art .hot.draw{stroke-dasharray:6 4;transition:stroke-dashoffset 1.4s ease}
  a:hover .art .hot.draw,.dpane .art .hot.draw{stroke-dashoffset:-80}
  .art .dot{fill:var(--amber);stroke:none}
  .art .ring{stroke:var(--amber);stroke-width:1.4}
  .art .rec{fill:#FF5A47;stroke:none}
  .art .tag{fill:#0E0F11;stroke:#3A4048}
  .art text{fill:#8A9099;stroke:none;font:500 8.5px 'IBM Plex Mono',ui-monospace,monospace;letter-spacing:.06em}
  .art text.ink{fill:#F1F2F4}
  .art text.sm{font-size:7.5px}
  .stage{position:relative;background:radial-gradient(85% 95% at 50% 40%,#1E2125,#121417);padding:14px 16px}
  .dcard,.drow{display:flex;flex-direction:column;border:1px solid var(--line);border-radius:14px;overflow:hidden;background:var(--bg2);transition:border-color .3s,transform .3s}
  .dcard:hover,.drow:hover{border-color:var(--line2);transform:translateY(-3px)}
  .dcard .stage{aspect-ratio:3/2;border-bottom:1px solid var(--line)}
  .body{padding:20px 22px 22px;display:flex;flex-direction:column;gap:8px;flex:1}
  .body h3,.dcard h3,.drow h3,.side h3{margin:0;font:300 22px var(--display)}
  .body p,.drow p,.side p{margin:0;color:var(--mute);font-size:14px}
  .meta{margin-top:auto;padding-top:14px;display:flex;justify-content:space-between;align-items:center;gap:12px;font-size:13.5px;border-top:1px solid var(--line);color:var(--ink2)}
  .meta b{font-weight:500;color:var(--ink)}
  .dA{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
  .dB{display:grid;grid-template-columns:1fr 1fr;gap:12px}
  .dcard.wide{flex-direction:row}
  .dcard.wide .stage{flex:0 0 55%;aspect-ratio:auto;min-height:260px;border-bottom:0;border-right:1px solid var(--line);display:grid;place-items:center}
  .dcard.wide ul,.side ul{list-style:none;margin:6px 0 0;padding:0;display:grid;gap:7px;font-size:13.5px;color:var(--ink2)}
  .dcard.wide li,.side li{display:flex;justify-content:space-between;gap:10px}
  .dcard.wide li b,.side li b{font:400 12.5px var(--mono);color:var(--ink)}
  .dC{display:grid;border-top:1px solid var(--line2)}
  .drow{flex-direction:row;align-items:center;gap:28px;border:0;border-bottom:1px solid var(--line);border-radius:0;background:none;padding:14px 0}
  .drow:hover{transform:none;background:var(--bg2)}
  .drow .stage{flex:0 0 240px;aspect-ratio:3/2;border-radius:10px;border:1px solid var(--line);padding:8px 10px}
  .drow .txt{flex:1;min-width:0}
  .drow h3{font-size:clamp(24px,2.6vw,34px);font-weight:200}
  .drow .num{flex:0 0 170px;font:200 34px/1 var(--display)}
  .drow .num small{display:block;font:400 10.5px var(--mono);letter-spacing:.12em;text-transform:uppercase;color:var(--mute);margin-top:8px}
  .drow .end{flex:0 0 170px;display:grid;gap:6px;text-align:right;font-size:13.5px;color:var(--ink2);padding-right:16px}
  .drow .end b{font-weight:500;color:var(--ink)}
  .dD{display:grid;grid-template-columns:260px minmax(0,1fr);border:1px solid var(--line);border-radius:16px;overflow:hidden;background:var(--bg2)}
  .dtabs{display:flex;flex-direction:column;padding:10px;border-right:1px solid var(--line);gap:2px}
  .dtabs button{text-align:left;background:none;border:0;border-radius:10px;padding:16px;cursor:pointer;font:300 18px var(--display);color:var(--mute);display:grid;gap:2px;transition:background .25s,color .25s}
  .dtabs button small{font:400 11.5px var(--mono);color:var(--mute)}
  .dtabs button[aria-selected="true"]{background:var(--surf);color:var(--ink);box-shadow:inset 2px 0 0 var(--amber)}
  .dpane{display:grid;grid-template-columns:minmax(0,1.5fr) minmax(0,1fr)}
  .stage.big{aspect-ratio:3/2;border-right:1px solid var(--line);padding:22px 26px}
  .side{padding:28px 28px 26px;display:flex;flex-direction:column;gap:8px}
  .side h3{font-size:30px;font-weight:200}
  .side .kn{font:200 56px/1 var(--display);margin:14px 0 8px}
  .side .cta{background:var(--ink);color:var(--bg);border-radius:9px;padding:10px 16px;font-weight:500;font-size:13.5px;white-space:nowrap}
  .dE{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
  .dcard.tall .stage{aspect-ratio:4/5;display:flex;flex-direction:column;justify-content:center}
  .dcard.tall .art{height:auto}
  .over{position:absolute;left:18px;right:18px;bottom:16px;display:flex;flex-direction:column}
  .over b{font:200 52px/1 var(--display);letter-spacing:-.02em;color:var(--ink)}
  .over span{font:400 10.5px var(--mono);letter-spacing:.12em;text-transform:uppercase;color:var(--mute);margin-top:6px}
  .dcard.tall .stage .art{margin-top:-20%}
  .arrow::after{content:" \\2192";display:inline-block;transition:transform .3s}
  a:hover .arrow::after{transform:translateX(4px)}
  @media (max-width:1100px){.dA,.dE{grid-template-columns:1fr 1fr}.dB{grid-template-columns:1fr}.drow .end{display:none}}
  @media (max-width:820px){
    .dcard.wide{flex-direction:column}.dcard.wide .stage{border-right:0;border-bottom:1px solid var(--line);min-height:0;aspect-ratio:3/2}
    .drow{flex-wrap:wrap;gap:12px 18px;padding:16px 0}.drow .stage{flex:1 1 100%}.drow .num{flex:1 1 100%;font-size:28px}
    .dD{grid-template-columns:1fr}.dtabs{flex-direction:row;overflow-x:auto;border-right:0;border-bottom:1px solid var(--line);scrollbar-width:none}.dtabs button{flex:0 0 auto;font-size:15px}
    .dpane{grid-template-columns:1fr}.stage.big{border-right:0;border-bottom:1px solid var(--line)}
  }
  @media (max-width:560px){.dA,.dE{grid-template-columns:1fr}}
  @media (prefers-reduced-motion:reduce){.art .hot.draw,.dcard,.drow{transition:none}}`;

  function build(root){
    if(root.dataset.desenhos==='A'){root.innerHTML=V[0].f();return}
    root.innerHTML=`<div class="dsw" role="group" aria-label="Variação do desenho"><span class="lbl">Variação</span>${V.map((v,i)=>`<button type="button" data-i="${i}" aria-pressed="${i===0}">${v.n}</button>`).join('')}</div>`+V.map((v,i)=>`<div class="dv" data-i="${i}"${i?' hidden':''}>${v.f()}</div>`).join('');
    root.querySelectorAll('.dsw button').forEach(b=>b.addEventListener('click',()=>{
      root.querySelectorAll('.dsw button').forEach(x=>x.setAttribute('aria-pressed',x===b));
      root.querySelectorAll('.dv').forEach(x=>x.hidden=x.dataset.i!==b.dataset.i)}));
    const p=root.querySelector('.dpane');pane(p,0);
    root.querySelectorAll('.dtabs button').forEach(b=>b.addEventListener('click',()=>{
      root.querySelectorAll('.dtabs button').forEach(x=>x.setAttribute('aria-selected',x===b));pane(p,+b.dataset.i)}));
  }
  const st=document.createElement('style');st.textContent=css;document.head.appendChild(st);
  const go=()=>{document.querySelectorAll('[data-desenhos]').forEach(build);document.querySelectorAll('[data-art]').forEach(el=>el.innerHTML=svg(el.dataset.art))};
  document.readyState==='loading'?document.addEventListener('DOMContentLoaded',go):go();
})();
