/* =====================================================================
   Protótipo 02: navbar (navbar.gallery) + efeitos Aceternity
   Uma função por efeito. Para desligar um, comente a chamada no fim.
   ===================================================================== */
(()=>{
const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];
const PRODUTOS=[
 {n:'Falcon X2 Pro',d:'Aerion · 4K 60 · R$ 12.499',img:'assets/img/falcon.webp',h:'produto.html'},
 {n:'Nimbus Mini 3',d:'Skyline · 249 g · R$ 4.290',img:'assets/img/nimbus.webp',h:'produto.html'},
 {n:'Heron Survey Pro',d:'Heron · RTK 1 cm · R$ 28.400',img:'assets/img/heron.webp',h:'produto.html'},
 {n:'Vortex FPV Racer',d:'Vortex · 120 km/h · R$ 3.150',img:'',h:'produto.html'},
 {n:'Bateria extra Falcon X2',d:'Acessório · R$ 2.380',img:'',h:'acessorios.html'},
 {n:'Filtros ND (kit 4)',d:'Acessório · R$ 349',img:'',h:'acessorios.html'},
];
const pre=location.pathname.includes('/admin/')?'../':'';

/* A. Announcement Bar */
function anuncio(){
  const nw=$('.navwrap');if(!nw||pre)return;
  try{if(sessionStorage.getItem('ac-ann'))return}catch(e){}
  const d=document.createElement('div');d.className='ac-ann';
  d.innerHTML=`<span><b>Frete grátis</b> acima de R$ 999<span class="ac-ann-x"> · 10% off na primeira compra com <code>PILOTO10</code></span> · <a href="ofertas.html">Ver ofertas</a></span><button type="button" aria-label="Fechar aviso">×</button>`;
  nw.before(d);$('button',d).onclick=()=>{d.remove();try{sessionStorage.setItem('ac-ann',1)}catch(e){}};
}

/* B. Navbar some ao descer / volta ao subir / encolhe (F, barra de progresso, removida a pedido) */
function rolagem(){
  const nw=$('.navwrap');
  let ult=scrollY;
  const f=()=>{const y=scrollY,max=document.documentElement.scrollHeight-innerHeight;
        if(nw){nw.classList.toggle('ac-small',y>40);
      const aberto=$('.links.open')||$('.msearch.open')||$('.ac-mega.on')||$('.ac-sug.on');
      nw.classList.toggle('ac-hide',!aberto&&y>200&&y>ult+4);if(y<ult-4)nw.classList.remove('ac-hide')}
    ult=y};
  addEventListener('scroll',f,{passive:true});f();
}

/* D. Mega menus de Drones e Acessórios (abrem no hover, um de cada vez) */
function megaMenu(href,html){
  const nav=$('.nav'),gat=$(`.links a[href$="${href}"]`);if(!nav||!gat)return;
  const m=document.createElement('div');m.className='ac-mega';m.innerHTML=html;nav.append(m);let t;
  const abre=()=>{clearTimeout(t);$$('.ac-mega.on').forEach(x=>x!==m&&x.classList.remove('on'));m.classList.add('on')},fecha=()=>{t=setTimeout(()=>m.classList.remove('on'),180)};
  [gat,m].forEach(e=>{e.addEventListener('mouseenter',abre);e.addEventListener('mouseleave',fecha)});
  gat.addEventListener('focus',abre);m.addEventListener('focusout',e=>{if(!m.contains(e.relatedTarget))fecha()});
  addEventListener('keydown',e=>{if(e.key==='Escape')m.classList.remove('on')});
}
function mega(){
  megaMenu('drones.html',`<div class="col"><span class="lbl">Por uso</span>
   <a href="${pre}drones.html">Cinema e criação<small>Câmera 4K/6K, gimbal 3 eixos</small></a>
   <a href="${pre}drones.html">Viagem e iniciantes<small>Até 249 g, sem registro</small></a>
   <a href="${pre}empresas.html">Mapeamento e agro<small>RTK, fotogrametria</small></a></div>
  <div class="col"><span class="lbl">Por marca</span>
   <a href="${pre}drones.html">Aerion<small>Linha Falcon</small></a>
   <a href="${pre}drones.html">Skyline<small>Linha Nimbus</small></a>
   <a href="${pre}drones.html">Heron<small>Linha Survey</small></a>
   <a href="${pre}drones.html">Vortex<small>FPV e corrida</small></a></div>
  <div class="col"><span class="lbl">Atalhos</span>
   <a href="${pre}ofertas.html">Ofertas da semana<small>Até 15% off</small></a>
   <a href="${pre}acessorios.html">Kits Fly More<small>Baterias, hélices, case</small></a>
   <a href="${pre}suporte.html">Ajuda para escolher<small>Fale com um piloto</small></a></div>
  <a class="feat" href="${pre}produto.html"><img src="${pre}assets/img/falcon.webp" alt=""><span class="lbl" style="color:var(--amber)">Lançamento</span><b>Falcon X2 Pro</b><span>4K 60 · 43 min · a partir de R$ 12.499</span></a>`);
  const A=`${pre}acessorios.html`;
  megaMenu('acessorios.html',`<div class="col"><span class="lbl">Energia</span>
   <a href="${A}">Baterias<small>Falcon X2, Nimbus Mini 3</small></a>
   <a href="${A}">Carregadores<small>Hub para 3 baterias</small></a>
   <a href="${A}">Hélices de reposição<small>Pares com encaixe rápido</small></a></div>
  <div class="col"><span class="lbl">Transporte e câmera</span>
   <a href="${A}">Maletas e mochilas<small>Maleta rígida e mochila 25 L</small></a>
   <a href="${A}">Filtros ND<small>Kit com 4 filtros</small></a>
   <a href="${A}">Controle e antenas<small>Aeria RC Pro, longo alcance</small></a></div>
  <div class="col"><span class="lbl">Por drone</span>
   <a href="${A}">Para Falcon X2<small>Aerion</small></a>
   <a href="${A}">Para Nimbus Mini 3<small>Skyline</small></a>
   <a href="${A}">Universais<small>Servem em qualquer modelo</small></a></div>
  <a class="feat" href="${A}"><img src="${pre}assets/img/falcon.webp" alt=""><span class="lbl" style="color:var(--amber)">Kit</span><b>Fly More Falcon X2</b><span>Drone + 2 baterias extras · economize na compra junta</span></a>`);
}

/* E. Busca com sugestões */
function sugestoes(){
  const lab=$('label.search'),inp=lab&&$('input',lab);if(!inp)return;
  const box=document.createElement('div');box.className='ac-sug';box.setAttribute('role','listbox');lab.append(box);let sel=-1;
  const esc=s=>s.replace(/[.*+?^${}()|[\]\\]/g,'\\$&');
  const draw=()=>{const q=inp.value.trim();const r=q?PRODUTOS.filter(p=>p.n.toLowerCase().includes(q.toLowerCase())):PRODUTOS.slice(0,4);sel=-1;
    box.innerHTML=(q?'':'<span class="lbl" style="padding:8px 10px 4px">Mais buscados</span>')+(r.length?r.map(p=>`<a href="${pre}${p.h}" role="option">${p.img?`<img src="${pre}${p.img}" alt="">`:'<span class="ph"></span>'}<span>${q?p.n.replace(new RegExp(esc(q),'i'),m=>`<mark>${m}</mark>`):p.n}<small>${p.d}</small></span></a>`).join(''):'<div class="vazio">Nada encontrado. Tente "Falcon" ou "bateria".</div>');
    box.classList.add('on')};
  inp.addEventListener('focus',draw);inp.addEventListener('input',draw);
  inp.addEventListener('blur',()=>setTimeout(()=>box.classList.remove('on'),150));
  inp.addEventListener('keydown',e=>{const as=$$('a',box);if(!as.length)return;
    if(e.key==='ArrowDown'||e.key==='ArrowUp'){e.preventDefault();sel=(sel+(e.key==='ArrowDown'?1:-1)+as.length)%as.length;as.forEach((a,i)=>a.classList.toggle('sel',i===sel))}
    if(e.key==='Enter'&&sel>=0){e.preventDefault();location.href=as[sel].href}});
}

/* G. Spotlight na capa */
function spotlight(){
  const h=$('.hero');if(!h)return;const s=document.createElement('div');s.className='ac-spot';s.setAttribute('aria-hidden','true');h.prepend(s);
  h.addEventListener('pointermove',e=>{const r=h.getBoundingClientRect();s.style.setProperty('--x',(e.clientX-r.left)+'px');s.style.setProperty('--y',(e.clientY-r.top)+'px')});
}

/* H. Moving Border no botão principal da capa */
function bordaGirando(){$$('.hero .actions .btn.p').forEach(b=>b.classList.add('ac-mb'))}

/* I. Card Spotlight */
function cardGlow(){
  $$('.card,.pcard').forEach(c=>{c.classList.add('ac-glow');c.addEventListener('pointermove',e=>{const r=c.getBoundingClientRect();c.style.setProperty('--x',(e.clientX-r.left)+'px');c.style.setProperty('--y',(e.clientY-r.top)+'px')})});
}

/* J. Infinite Moving Cards: depoimentos (home) */
function depoimentos(){
  const alvo=$('section.trust');if(!alvo)return;
  const D=[
   ['Rafael M.','Videomaker · Palmas','O Falcon chegou em 2 dias e a imagem em 4K 60 é absurda. Atendimento me ajudou a escolher o kit certo.'],
   ['Juliana S.','Agrônoma','Usamos o Heron com RTK no mapeamento das fazendas. Precisão de centímetro, como prometido.'],
   ['Pedro A.','Iniciante','Comecei pelo Nimbus Mini, que não precisa de registro. Leve, estável e fácil de pilotar.'],
   ['Carla R.','Fotógrafa de casamentos','O gimbal segura até com vento. Paguei no Pix com desconto e veio tudo certinho.'],
   ['Lucas T.','Piloto FPV','O Vortex é rápido demais. Peças de reposição chegam rápido, isso faz diferença.'],
   ['Marina R.','Corretora de imóveis','Fotos aéreas dos imóveis venderam mais rápido. Suporte respondeu no mesmo dia.'],
  ];
  const card=([n,p,t])=>`<div class="t"><div class="stars">★★★★★</div><p>${t}</p><div class="who"><span class="av">${n.split(' ').map(x=>x[0]).join('')}</span><span>${n}<small>${p}</small></span></div></div>`;
  const a=D.map(card).join(''),b=[...D].reverse().map(card).join('');
  const s=document.createElement('section');s.className='ac-test';s.setAttribute('aria-labelledby','acTestT');
  s.innerHTML=`<div class="wrap"><span class="lbl">Quem já voa com a gente</span><h2 id="acTestT">Pilotos que recomendam.</h2></div>
   <div class="mask"><div class="row">${a}<span aria-hidden="true" style="display:contents">${a}</span></div><div class="row r2">${b}<span aria-hidden="true" style="display:contents">${b}</span></div></div>`;
  alvo.before(s);
  /* celular: setas e troca automática a cada 30 s (para quando a pessoa mexe) */
  const m=$('.mask',s),seta=document.createElement('div');seta.className='ac-test-setas';
  seta.innerHTML='<button type="button" aria-label="Depoimento anterior">‹</button><button type="button" aria-label="Próximo depoimento">›</button>';
  m.after(seta);const [ant,prox]=seta.children;
  const passo=()=>{const c=$('.t',m);return c?c.getBoundingClientRect().width+12:m.clientWidth};
  const fim=()=>m.scrollLeft>=m.scrollWidth-m.clientWidth-2;
  const estado=()=>{ant.disabled=m.scrollLeft<=2;prox.disabled=fim()};
  ant.onclick=()=>{m.scrollBy({left:-passo(),behavior:'smooth'});reinicia()};
  prox.onclick=()=>{m.scrollBy({left:passo(),behavior:'smooth'});reinicia()};
  m.addEventListener('scroll',estado,{passive:true});estado();
  let timer;const celular=matchMedia('(max-width:640px)');
  const avanca=()=>{if(!celular.matches)return;m.scrollTo({left:fim()?0:m.scrollLeft+passo(),behavior:'smooth'})};
  const reinicia=()=>{clearInterval(timer);timer=setInterval(avanca,30000)};
  m.addEventListener('touchstart',reinicia,{passive:true});reinicia();
}

anuncio();
rolagem();
mega();
sugestoes();
spotlight();
bordaGirando();
cardGlow();
depoimentos();
})();
