/* =====================================================================
   Protótipo Spell: efeitos inspirados nos componentes de spell.sh/docs
   Uma função por efeito. Para desligar um, comente a chamada no fim.
   ===================================================================== */
(()=>{
const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];

/* Vitrine: libera as animações mesmo com "reduzir movimento" ligado no sistema */
function liberarMovimento(){
  for(const ss of document.styleSheets){let rs;try{rs=ss.cssRules}catch(e){continue}
    for(let i=rs.length-1;i>=0;i--){const r=rs[i];if(r.media&&/prefers-reduced-motion/.test(r.media.mediaText))ss.deleteRule(i)}}
}

/* 1. Blur Reveal: título das capas (home, empresas, ofertas) entra palavra por palavra */
function blurReveal(){
  const alvos=$$('.hero h1, .bhero h1, .ohero h1'); // só nas capas: home, empresas e ofertas
  alvos.forEach(h=>{let i=0;
    const walk=n=>{[...n.childNodes].forEach(c=>{
      if(c.nodeType===3&&c.textContent.trim()){const f=document.createDocumentFragment();
        c.textContent.split(/(\s+)/).forEach(p=>{if(!p)return;if(/^\s+$/.test(p)){f.append(p);return}
          const s=document.createElement('sp-w');s.style.setProperty('--i',i++);s.textContent=p;f.append(s)});
        c.replaceWith(f)}
      else if(c.nodeType===1&&c.tagName!=='BR'&&!c.matches('svg,sp-w'))walk(c)})};
    walk(h)});
  const io=new IntersectionObserver(es=>es.forEach(e=>{if(e.isIntersecting){e.target.classList.add('sp-in');io.unobserve(e.target)}}),{threshold:.2});
  alvos.forEach(h=>io.observe(h));
}

/* 2. Shimmer Text: selos e preço promocional */
function shimmer(){
  $$('.chip, .pr.sale').forEach(el=>{
    const t=[...el.childNodes].find(n=>n.nodeType===3&&n.textContent.trim());if(!t)return;
    const s=document.createElement('span');s.className='sp-sh';s.textContent=t.textContent;t.replaceWith(s)});
}

/* 3. Marquee: faixa de marcas e garantias logo abaixo da ficha rápida (home) */
function marquee(){
  const tele=$('section.tele');if(!tele)return;
  const itens=[['AERION','drones'],['SKYLINE','compactos'],['HERON','RTK'],['VORTEX','FPV'],['FRETE GRÁTIS','acima de R$ 999'],['12× SEM JUROS','no cartão'],['5% OFF','no Pix'],['GARANTIA','2 anos']];
  const um=itens.map(([a,b])=>`<span class="it">${a}<small>${b}</small></span><span class="dot"></span>`).join('');
  const sec=document.createElement('section');sec.className='sp-marq';sec.setAttribute('aria-label','Marcas e vantagens');
  sec.innerHTML=`<div class="trk">${um}<span aria-hidden="true" style="display:contents">${um}</span></div>`;
  tele.after(sec);
}

/* 5. Pop Button: sacola dos cards pula, solta faíscas e soma no contador */
function popBag(){
  const cont=$('a[href="carrinho.html"] .navcount');
  const toast=document.createElement('div');toast.className='sp-toast';toast.setAttribute('role','status');document.body.append(toast);let tt;
  $$('a.btn.sq[aria-label="Adicionar ao carrinho"]').forEach(b=>b.addEventListener('click',e=>{
    e.preventDefault();b.classList.remove('sp-pop');void b.offsetWidth;b.classList.add('sp-pop');
    const r=b.getBoundingClientRect(),cx=r.left+r.width/2,cy=r.top+r.height/2;
    for(let k=0;k<8;k++){const s=document.createElement('i');s.className='sp-spark';const a=k/8*Math.PI*2;
      s.style.cssText=`left:${cx-3}px;top:${cy-3}px;--dx:${Math.cos(a)*34}px;--dy:${Math.sin(a)*34}px`;document.body.append(s);setTimeout(()=>s.remove(),650)}
    if(cont){cont.textContent=+cont.textContent+1;cont.classList.remove('sp-bump');void cont.offsetWidth;cont.classList.add('sp-bump')}
    const nome=b.closest('.card,article')?.querySelector('h3')?.textContent||'Item';
    toast.textContent=`${nome} adicionado à sacola`;toast.classList.add('on');clearTimeout(tt);tt=setTimeout(()=>toast.classList.remove('on'),1800)}));
}

/* 8. Spinner: botões que processam mostram carregamento antes de seguir */
function spinner(){
  const aplicar=(b,seguir)=>{if(b.dataset.spOk){delete b.dataset.spOk;return false}
    const orig=b.innerHTML;b.classList.add('sp-busy');b.innerHTML=`<span class="sp-spin"></span>Processando…`;
    setTimeout(()=>{b.innerHTML=orig;b.classList.remove('sp-busy');seguir()},1100);return true};
  $$('button.btn.p[type="submit"]').forEach(b=>b.addEventListener('click',e=>{
    if(aplicar(b,()=>{b.dataset.spOk=1;b.click()})){e.preventDefault();e.stopImmediatePropagation()}},true));
  $$('a.btn.p[href="confirmacao.html"]').forEach(a=>a.addEventListener('click',e=>{e.preventDefault();aplicar(a,()=>location.href=a.href)}));
}

/* 9. Light Rays: feixes atrás do drone da capa */
function lightRays(){
  const h=$('.hero');if(!h)return;const d=document.createElement('div');d.className='sp-rays';d.setAttribute('aria-hidden','true');
  const ref=$('.glow',h)||h.firstChild;ref.after(d);
}

/* 10. QR Code + Copy Button: ao escolher Pix no checkout */
function pix(){
  const p=$('#ppix');if(!p)return;
  const codigo='00020126580014BR.GOV.BCB.PIX0136aeria-drones-prototipo-0000-0000520400005303986540514422.005802BR5912AERIA DRONES6006PALMAS62070503***6304A1B2';
  const box=document.createElement('div');box.className='sp-pix';
  box.innerHTML=`<div class="sp-qr" aria-label="QR Code do Pix (ilustrativo)"><span class="sp-spin"></span></div>
  <div><b style="font-weight:500">Pix copia e cola</b><p class="formnote" style="margin:4px 0 0">QR ilustrativo do protótipo. Expira em 30 min.</p>
  <div class="sp-code"><code>${codigo}</code><button class="btn sm sp-copy" type="button"><span class="go">Copiar</span><span class="ok">✓ Copiado</span></button></div></div>`;
  p.append(box);
  const qr=$('.sp-qr',box);let feito=false;
  const gerar=()=>{if(feito)return;feito=true;setTimeout(()=>{qr.innerHTML='';qr.append(qrFalso(codigo))},900)};
  new MutationObserver(()=>{if(!p.hidden)gerar()}).observe(p,{attributes:true,attributeFilter:['hidden']});if(!p.hidden)gerar();
  const btn=$('.sp-copy',box);btn.addEventListener('click',()=>{navigator.clipboard?.writeText(codigo).catch(()=>{});btn.classList.add('done');setTimeout(()=>btn.classList.remove('done'),1800)});
}
/* QR só visual: padrão determinístico a partir do texto, com os 3 quadros de posição */
function qrFalso(txt){
  const N=29,c=document.createElement('canvas');c.width=c.height=N;const g=c.getContext('2d');g.fillStyle='#fff';g.fillRect(0,0,N,N);g.fillStyle='#111';
  let h=2166136261;const rnd=()=>{for(const ch of txt)h=Math.imul(h^ch.charCodeAt(0),16777619);h^=h>>>13;return (h>>>0)/4294967296};
  const fin=(x,y)=>{g.fillRect(x,y,7,7);g.clearRect(x+1,y+1,5,5);g.fillStyle='#fff';g.fillRect(x+1,y+1,5,5);g.fillStyle='#111';g.fillRect(x+2,y+2,3,3)};
  const emFin=(x,y)=>(x<8&&y<8)||(x>N-9&&y<8)||(x<8&&y>N-9);
  for(let y=0;y<N;y++)for(let x=0;x<N;x++)if(!emFin(x,y)&&rnd()>.52)g.fillRect(x,y,1,1);
  fin(0,0);fin(N-7,0);fin(0,N-7);return c;
}

/* 11. Randomized Text: números da ficha rápida sorteiam até o valor final */
function randomized(){
  const els=$$('.tele .read strong');if(!els.length)return;
  els.forEach(st=>{const t=[...st.childNodes].find(n=>n.nodeType===3);if(!t)return;
    const s=document.createElement('sp-rnd');s.dataset.v=t.textContent;s.textContent=t.textContent;t.replaceWith(s)});
  const io=new IntersectionObserver(es=>es.forEach(e=>{if(!e.isIntersecting)return;io.unobserve(e.target);
    const s=$('sp-rnd',e.target);if(!s)return;const fim=s.dataset.v,dig='0123456789';let k=0;
    const id=setInterval(()=>{k++;const fixos=Math.floor(k/3);
      s.textContent=[...fim].map((ch,i)=>i<fixos||!/\d/.test(ch)?ch:dig[Math.random()*10|0]).join('');
      if(fixos>=fim.length){clearInterval(id);s.textContent=fim}},45)}),{threshold:.6});
  els.forEach(e=>io.observe(e));
}

/* 15. Hélices girando nos drones de empresas (Heron) e da oferta relâmpago (Nimbus):
   foto sem as pás + disco em SVG sobre cada motor, como no Falcon da home */
function helices(){
  const ROT={
    heron:{w:1375,h:700,r:[[405,45,245,70,-13,1],[1160,130,225,80,22,-1],[160,275,195,80,33,-1],[1060,385,315,75,-11,1]]},
    nimbus:{w:1383,h:626,r:[[460,45,230,65,-12,1],[1200,195,200,75,30,-1],[115,250,160,75,42,-1],[955,400,300,70,-10,1]]},
  };
  const sv=(t,a)=>{const e=document.createElementNS('http://www.w3.org/2000/svg',t);for(const k in a)e.setAttribute(k,a[k]);return e};
  const giro=(de,ate,dur)=>sv('animateTransform',{attributeName:'transform',type:'rotate',from:de,to:ate,dur,repeatCount:'indefinite'});
  const pa=L=>{const w=L*.085;return `M0,-${w*.45} C${L*.25},-${w} ${L*.7},-${w*1.05} ${L*.97},-${w*.5} Q${L},0 ${L*.97},${w*.35} C${L*.7},${w*.55} ${L*.25},${w*.6} 0,${w*.45}Z`};
  $$('.bhero img[src*="heron"], .deal .stage img[src*="nimbus"]').forEach((img,idx)=>{
    const nome=img.getAttribute('src').includes('heron')?'heron':'nimbus',C=ROT[nome];
    const wrap=document.createElement('div');wrap.className='sp-drone';img.replaceWith(wrap);
    img.src=img.getAttribute('src').replace(nome+'.webp',nome+'-sem-pas.webp');wrap.append(img);
    const s=sv('svg',{viewBox:`0 0 ${C.w} ${C.h}`,class:'sp-rot','aria-hidden':'true'});
    const g0=`dg${idx}`,b0=`bl${idx}`;const defs=sv('defs',{});
    const rg=sv('radialGradient',{id:g0});[[0,.05],[.55,.13],[.92,.2],[1,0]].forEach(([o,a])=>rg.append(sv('stop',{offset:o,'stop-color':'#d4d7dc','stop-opacity':a})));
    const lg=sv('linearGradient',{id:b0,x1:0,y1:0,x2:0,y2:1});[['0','#6b6e74'],['.5','#45484d'],['1','#2c2e32']].forEach(([o,c])=>lg.append(sv('stop',{offset:o,'stop-color':c})));
    defs.append(rg,lg);s.append(defs);
    C.r.forEach(([cx,cy,rx,ry,t,d],i)=>{const k=ry/rx;
      const g=sv('g',{transform:`translate(${cx} ${cy}) rotate(${t}) scale(1 ${k})`});
      g.append(sv('circle',{r:rx,style:`fill:url(#${g0});stroke:none`}));
      const ar=sv('g',{});ar.append(giro('0',String(360*d),'0.18s'));
      [[1,1],[-1,-1]].forEach(([sx,sy])=>ar.append(sv('path',{d:`M${sx*rx*.96},0 A${rx*.96},${rx*.96} 0 0 1 ${-sx*rx*.5},${sy*rx*.82}`,style:`fill:none;stroke:rgba(230,232,236,.22);stroke-width:${rx*.07}`})));
      g.append(ar);
      const bl=sv('g',{style:'opacity:.28;filter:blur(1.5px)'});bl.append(giro(String(i*40),String(i*40+360*d),'0.9s'));
      [0,180].forEach(a=>bl.append(sv('path',{d:pa(rx),transform:`rotate(${a})`,style:`fill:url(#${b0});stroke:none`})));
      g.append(bl);
      const hub=sv('g',{transform:`scale(1 ${1/k})`});
      hub.append(sv('ellipse',{rx:rx*.06,ry:rx*.06*k*1.6,style:'fill:#2a2c30;stroke:#55585e;stroke-width:2'}));
      g.append(hub);s.append(g)});
    wrap.append(s);
    const o=document.createElement('img');o.className='sp-parado';o.alt='';o.src=img.src.replace('-sem-pas','');wrap.append(o);
    /* depois que as pás param, a camada das hélices sai da página: nada fica girando escondido */
    s.addEventListener('animationend',()=>s.remove(),{once:true});setTimeout(()=>s.remove(),4200)});
}

/* 16. Números da faixa de ofertas (22%, 48, 7 dias) contam a partir de zero ao aparecer */
function contar(){
  const els=$$('.ostats b');if(!els.length)return;
  const io=new IntersectionObserver(es=>es.forEach(e=>{if(!e.isIntersecting)return;io.unobserve(e.target);
    const b=e.target,txt=b.textContent,m=txt.match(/^(\d+)(.*)$/);if(!m)return;const fim=+m[1],suf=m[2],t0=performance.now(),dur=1200;
    const passo=t=>{const p=Math.min(1,(t-t0)/dur),v=Math.round(fim*(1-Math.pow(1-p,3)));b.textContent=v+suf;if(p<1)requestAnimationFrame(passo)};
    b.textContent='0'+suf;requestAnimationFrame(passo)}),{threshold:.6});
  els.forEach(e=>io.observe(e));
}

liberarMovimento();
blurReveal();
/* shimmer(); removido a pedido: brilho nos selos não aprovado */
/* marquee(); removido a pedido: faixa de marcas não aprovada */
popBag();
spinner();
lightRays();
pix();
randomized();
helices();
contar();
/* 4 (Flow Button), 6 (Animated Checkbox) e 7 (Label Input) são só CSS, em spell.css.
   Tilt Card já está em conta-pagamentos.html. */
})();
