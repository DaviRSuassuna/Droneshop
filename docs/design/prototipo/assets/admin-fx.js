/* =====================================================================
   Painel do administrador: animações e interações dos gráficos
   (protótipo 02). Uma função por efeito; comente a chamada no fim para desligar.
   ===================================================================== */
(()=>{
const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];

/* A. Entrada em cascata de indicadores e cards */
function entrada(){
  [...$$('.kpi'),...$$('main .card')].forEach((el,i)=>{el.style.setProperty('--d',i);el.classList.add('fx-in')});
}

/* Números contam até o valor (KPIs: R$ 1,42 mi, 312, R$ 4.551, 2,7%) */
function contarKpis(){
  $$('.kpi strong').forEach(st=>{
    const t=[...st.childNodes].find(n=>n.nodeType===3&&/\d/.test(n.textContent));if(!t)return;
    const orig=t.textContent,m=orig.match(/^(\D*)([\d.,]+)(.*)$/);if(!m)return;
    const [_,pre,num,suf]=m,dec=num.includes(',')?num.split(',')[1].length:0;
    const fim=parseFloat(num.replace(/\./g,'').replace(',','.')),t0=performance.now()+200,dur=1400;
    const fmt=v=>pre+v.toLocaleString('pt-BR',{minimumFractionDigits:dec,maximumFractionDigits:dec})+suf;
    const passo=now=>{const p=Math.max(0,Math.min(1,(now-t0)/dur)),e=1-Math.pow(1-p,4);t.textContent=fmt(fim*e);if(p<1)requestAnimationFrame(passo);else t.textContent=orig};
    t.textContent=fmt(0);requestAnimationFrame(passo)});
}

/* B. Linhas desenhadas (gráfico principal e minigráficos) */
function desenhar(){
  $$('.chart .ln, .spark path').forEach((p,i)=>{const L=Math.ceil(p.getTotalLength());p.style.setProperty('--len',L);
    p.style.setProperty('--dl',p.closest('.spark')?(.4+i*.12)+'s':'.5s');p.classList.add('fx-draw')});
  $$('.chart .pt').forEach(p=>p.classList.add('fx-pulse'));
}

/* C. Gráfico de vendas: leitura no hover + alternar Receita/Pedidos */
function graficoVendas(){
  const svg=$('svg.chart');if(!svg)return;
  const ln=$('.ln',svg),ln2=$('.ln2',svg),area=$('.area',svg),pt=$('.pt',svg);
  const pts=d=>[...d.matchAll(/([\d.]+) ([\d.]+)/g)].map(m=>[+m[1],+m[2]]);
  const P=pts(ln.getAttribute('d')),P2=ln2?pts(ln2.getAttribute('d')):[];
  const Y0=212,Y1=14;let modo='receita';
  const toV=y=>(Y0-y)/(Y0-Y1);               // 0..1 da escala
  const ini=new Date(2026,8,20);
  const dia=i=>{const d=new Date(ini);d.setDate(d.getDate()+i);return d.toLocaleDateString('pt-BR',{weekday:'short',day:'2-digit',month:'2-digit'})};
  const val=(y,i)=>modo==='receita'?`R$ ${(toV(y)*100).toLocaleString('pt-BR',{maximumFractionDigits:1})} mil`:`${Math.round(toV(y)*100*1000/4551*(.9+((i*7)%5)/25))} pedidos`;
  // camada de leitura
  const NS='http://www.w3.org/2000/svg',mk=(t,a)=>{const e=document.createElementNS(NS,t);for(const k in a)e.setAttribute(k,a[k]);return e};
  const guia=mk('line',{class:'fx-guide',y1:Y1,y2:Y0}),dot2=mk('circle',{class:'fx-dot2',r:3}),dot=mk('circle',{class:'fx-dot',r:5});
  svg.append(guia,dot2,dot);
  const bd=svg.parentElement;bd.classList.add('fx-wrap');const tip=document.createElement('div');tip.className='fx-tip';bd.append(tip);
  const mover=e=>{const r=svg.getBoundingClientRect(),vb=svg.viewBox.baseVal,x=(e.clientX-r.left)/r.width*vb.width;
    const cur=pts(ln.getAttribute('d'));let i=0,best=1e9;cur.forEach(([px],k)=>{const dd=Math.abs(px-x);if(dd<best){best=dd;i=k}});
    const [px,py]=cur[i],q=pts(ln2.getAttribute('d'))[i];
    guia.setAttribute('x1',px);guia.setAttribute('x2',px);dot.setAttribute('cx',px);dot.setAttribute('cy',py);
    if(q){dot2.setAttribute('cx',q[0]);dot2.setAttribute('cy',q[1])}
    const delta=q?((toV(py)-toV(q[1]))/Math.max(.01,toV(q[1]))*100):0;
    tip.innerHTML=`<span class="d">${dia(i).toUpperCase()}</span><b>${val(py,i)}</b><span class="v${delta<0?' n':''}">${delta>=0?'▲':'▼'} ${Math.abs(delta).toFixed(1).replace('.',',')}%</span> vs. período anterior`;
    const bx=svg.getBoundingClientRect(),bb=bd.getBoundingClientRect();
    tip.style.left=Math.min(bb.width-90,Math.max(90,(px/vb.width)*bx.width+(bx.left-bb.left)))+'px';
    tip.style.top=Math.max(0,(py/vb.height)*bx.height+(bx.top-bb.top)-78)+'px';bd.classList.add('fx-on')};
  svg.addEventListener('pointermove',mover);svg.addEventListener('pointerleave',()=>bd.classList.remove('fx-on'));
  // Receita x Pedidos: a curva se transforma e a escala muda
  const eixo=$$('text.ax',svg).filter(t=>t.getAttribute('text-anchor')==='end');
  const rotRec=eixo.map(t=>t.textContent),rotPed=['0','6','11','16','22'];
  const caminho=(arr,fecha)=>{let d=arr.map(([x,y],k)=>(k?'L':'M')+x.toFixed(1)+' '+y.toFixed(1)).join(' ');if(fecha)d+=` L${arr[arr.length-1][0]} ${Y0} L${arr[0][0]} ${Y0} Z`;return d};
  const forma=(arr,f)=>arr.map(([x,y],k)=>[x,Y0-(Y0-y)*f(k)]);
  const fPed=k=>.86+((k*7)%5)/22;
  const aplicar=m=>{modo=m;const f=m==='receita'?()=>1:fPed;
    const a=forma(P,f),b=forma(P2,f);
    ln.style.d=`path("${caminho(a)}")`;area.style.d=`path("${caminho(a,true)}")`;if(ln2)ln2.style.d=`path("${caminho(b)}")`;
    const u=a[a.length-1];pt.setAttribute('cy',u[1]);
    eixo.forEach((t,k)=>{t.style.opacity=0;setTimeout(()=>{t.textContent=m==='receita'?rotRec[k]:rotPed[k];t.style.opacity=1},250)})};
  $$('.seg button',svg.closest('.card')).forEach(b=>b.addEventListener('click',()=>{
    $$('.seg button',svg.closest('.card')).forEach(x=>x.setAttribute('aria-pressed',x===b));
    aplicar(/pedido/i.test(b.textContent)?'pedidos':'receita')}));
}

/* D. Barras crescem; estoque zerado pisca */
function barras(){
  $$('.meter i').forEach((i,k)=>i.style.setProperty('--d',k));
  $$('td div[style*="background:var(--amber)"]').forEach((b,k)=>{b.classList.add('fx-bar');b.style.setProperty('--d',k)});
  $$('.stock.low').forEach(s=>{if(/^0\b/.test(s.textContent.trim()))s.classList.add('fx-zero')});
}

/* F. Linhas de tabela em cascata (todas as telas do admin) */
function linhas(){$$('main tbody tr').forEach((tr,k)=>{tr.classList.add('fx-row');tr.style.setProperty('--d',Math.min(k,14))})}

/* G. Brilho na borda dos cards */
function brilho(){
  $$('main .card').forEach(c=>{c.classList.add('fx-glow');c.addEventListener('pointermove',e=>{const r=c.getBoundingClientRect();c.style.setProperty('--x',(e.clientX-r.left)+'px');c.style.setProperty('--y',(e.clientY-r.top)+'px')})});
}

/* H. Card novo no painel: receita por categoria (rosca + legenda com valores) */
function rosca(){
  const alvo=$$('main .card').find(c=>/Mais vendidos/.test(c.textContent));if(!alvo)return;
  const D=[['Lazer e criação',54,'#E8A86B','R$ 767 mil'],['Profissionais',35,'#C9CDD3','R$ 497 mil'],['Peças e energia',11,'#6E7580','R$ 156 mil']];
  const R=70,C=2*Math.PI*R,GAP=6;let acc=0;
  const segs=D.map(([n,p,c],i)=>{const l=C*p/100,s=`<circle class="seg-a" data-i="${i}" r="${R}" cx="90" cy="90" stroke="${c}" style="--l:${(l-GAP).toFixed(1)};--d:${i};stroke-dashoffset:${(-acc).toFixed(1)}"/>`;acc+=l;return s}).join('');
  const card=document.createElement('section');card.className='card fx-card-cat';
  card.innerHTML=`<div class="hd"><h2>Receita por categoria</h2><span class="lbl">Últimos 30 dias</span></div><div class="bd"><div class="fx-donut">
   <div class="mid"><svg viewBox="0 0 180 180" aria-hidden="true"><circle r="${R}" cx="90" cy="90" class="trk"/>${segs}</svg><div class="ctr"><span>TOTAL</span><b>R$ 1,42 mi</b><em>▲ 12,4%</em></div></div>
   <ul>${D.map(([n,p,c,v],i)=>`<li data-i="${i}"><i style="background:${c}"></i><div class="nm"><span>${n}</span><div class="br"><b style="--w:${p}%;background:${c}"></b></div></div><div class="vl"><strong>${v}</strong><small>${p}%</small></div></li>`).join('')}</ul></div></div>`;
  alvo.after(card);
  const dn=$('.fx-donut',card);
  $$('li',card).forEach(li=>{li.onmouseenter=()=>{dn.classList.add('hl');$$('circle.seg-a',card).forEach(c=>c.classList.toggle('on',c.dataset.i===li.dataset.i))};
    li.onmouseleave=()=>{dn.classList.remove('hl')}});
}

/* I. "Ao vivo" ao lado do título do painel */
function aoVivo(){const h=$('main .ph h1');if(h&&/Visão geral/.test(h.textContent)){const s=document.createElement('span');s.className='fx-live';s.textContent='ATUALIZADO AGORA';h.append(s)}}

/* K. Barra do admin agrupada, no estilo da loja: Dashboard · Catálogo · Vendas · Administração */
function menuAdmin(){
  const tabs=$('.tb .tabs');if(!tabs)return;
  const pg=location.pathname.split('/').pop()||'index.html';
  const G=[
   {n:'Dashboard',h:'index.html',p:['index.html']},
   {n:'Catálogo',cols:[['Produtos',[['produtos.html','Produtos','Drones, acessórios e kits',['produtos.html','produto-novo.html','acessorio-novo.html','kit-novo.html']],['grupos.html','Grupos de produto','Versões do mesmo modelo juntas',['grupos.html','grupo-novo.html']],['marcas.html','Marcas','Fabricantes e país de origem',['marcas.html','marca-nova.html']]]],
     ['Organização',[['categorias.html','Categorias','Menu e ordem de exibição',['categorias.html','categoria-nova.html']],['subcategorias.html','Subcategorias','Dentro de cada categoria',['subcategorias.html','subcategoria-nova.html']]]]],
     q:[['produto-novo.html','Novo drone'],['acessorio-novo.html','Novo acessório'],['kit-novo.html','Novo kit'],['grupo-novo.html','Novo grupo de produto']],stat:['148','itens no catálogo']},
   {n:'Vendas',cols:[['Vendas',[['pedidos.html','Pedidos','Pagamento, separação e entrega',['pedidos.html','pedido.html']]]],
     ['Ofertas',[['promocoes.html','Promoções','Descontos por período e produto',['promocoes.html','promocao-nova.html']],['cupons.html','Cupons','Códigos de desconto, como PILOTO10',['cupons.html','cupom-novo.html']]]],
     ['Compras',[['fornecedores.html','Fornecedores','CNPJ, contato e endereço',['fornecedores.html','fornecedor-novo.html']]]]],
     q:[['promocao-nova.html','Nova promoção'],['cupom-novo.html','Novo cupom'],['fornecedor-novo.html','Novo fornecedor']],stat:['18','pedidos aguardando ação']},
   {n:'Administração',cols:[['Operações',[['estoque.html','Posição de estoque','Disponível e mínimo por produto',['estoque.html']],['estoque-movimento.html','Movimentações','Entradas, saídas e ajustes',['estoque-movimento.html']]]],
     ['Administração',[['usuarios.html','Usuários','Equipe com acesso ao painel',['usuarios.html','usuario-novo.html']],['configuracao.html','Configuração da loja','Parâmetros gerais',['configuracao.html']]]]],
     q:[['estoque-movimento.html','Novo movimento'],['usuario-novo.html','Novo usuário administrativo']],stat:['4','produtos abaixo do mínimo']},
  ];
  const ativo=g=>g.p?g.p.includes(pg):g.cols.some(([,ls])=>ls.some(l=>l[3].includes(pg)));
  tabs.innerHTML='';tabs.classList.add('fx-groups');
  const pill=document.createElement('span');pill.className='fx-pill';tabs.append(pill);
  const barra=$('.tb .in');const menus=[];let tm;
  const fechaTodos=()=>{menus.forEach(([b,m])=>{b.classList.remove('open');b.setAttribute('aria-expanded','false');m.classList.remove('on')})};
  G.forEach(g=>{
    if(g.h){const a=document.createElement('a');a.href=g.h;a.textContent=g.n;if(ativo(g))a.setAttribute('aria-current','page');tabs.append(a);return}
    const b=document.createElement('button');b.type='button';b.textContent=g.n;b.setAttribute('aria-expanded','false');if(ativo(g))b.classList.add('on');tabs.append(b);
    const m=document.createElement('div');m.className='fx-menu';m.style.setProperty('--cols',g.cols.length);
    m.innerHTML=g.cols.map(([t,ls])=>`<div class="col"><span class="lbl">${t}</span>${ls.map(([h,n,d,ps])=>`<a href="${h}"${ps.includes(pg)?' aria-current="page"':''}>${n}<small>${d}</small></a>`).join('')}</div>`).join('')+
      `<div class="quick"><span class="lbl">Ações rápidas</span><div class="num">${g.stat[0]}<small>${g.stat[1]}</small></div>${g.q.map(([h,t])=>`<a href="${h}">${t}</a>`).join('')}</div>`;
    barra.append(m);menus.push([b,m]);
    const abre=()=>{clearTimeout(tm);fechaTodos();b.classList.add('open');b.setAttribute('aria-expanded','true');m.classList.add('on')};
    const fecha=()=>{tm=setTimeout(fechaTodos,200)};
    [b,m].forEach(e=>{e.addEventListener('mouseenter',abre);e.addEventListener('mouseleave',fecha)});
    b.addEventListener('click',()=>m.classList.contains('on')?fechaTodos():abre());
    m.addEventListener('mouseenter',()=>clearTimeout(tm));
  });
  addEventListener('keydown',e=>{if(e.key==='Escape')fechaTodos()});
  document.addEventListener('click',e=>{if(!barra.contains(e.target))fechaTodos()});
  const itens=[...tabs.children].filter(x=>x!==pill),atual=itens.find(x=>x.getAttribute('aria-current')||x.classList.contains('on'));
  const ir=x=>{if(!x){pill.style.opacity=0;return}pill.style.left=x.offsetLeft+'px';pill.style.width=x.offsetWidth+'px';pill.style.opacity=1};
  itens.forEach(x=>x.addEventListener('mouseenter',()=>ir(x)));tabs.addEventListener('mouseleave',()=>ir(atual));setTimeout(()=>ir(atual),60);
}

menuAdmin();
rosca();
aoVivo();
entrada();
contarKpis();
desenhar();
graficoVendas();
barras();
linhas();
brilho();
})();
