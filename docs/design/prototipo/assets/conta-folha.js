/* Conta no celular: o ícone da conta abre uma folha de baixo com as áreas da conta.
   Tocar fora, arrastar para baixo ou Esc fecha. No desktop o ícone continua levando direto ao perfil. */
(function(){
const $$=(s,r=document)=>[...r.querySelectorAll(s)];
// conta no celular: o ícone abre uma folha de baixo com as áreas da conta (arrastar para baixo ou tocar fora fecha)
const SV=p=>`<svg viewBox="0 0 24 24" aria-hidden="true">${p}</svg>`;
const AREAS=[['conta-pedidos.html','Meus pedidos','12','<path d="M4 7h16l-1.5 12h-13z"/><path d="M8 7V5a4 4 0 0 1 8 0v2"/>'],
  ['conta-perfil.html','Perfil e dados','','<circle cx="12" cy="8" r="4"/><path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6"/>'],
  ['conta-favoritos.html','Favoritos','6','<path d="M12 20s-7-4.5-7-10a4 4 0 0 1 7-2.5A4 4 0 0 1 19 10c0 5.5-7 10-7 10z"/>'],
  ['conta-enderecos.html','Endereços','2','<path d="M12 21s-7-6.5-7-12a7 7 0 0 1 14 0c0 5.5-7 12-7 12z"/><circle cx="12" cy="9" r="2.5"/>'],
  ['conta-pagamentos.html','Meios de pagamento','2','<rect x="3" y="6" width="18" height="13" rx="2"/><path d="M3 10h18"/>']];
const aqui=location.pathname.split('/').pop();
const celular=matchMedia('(max-width:900px)');let folha;
const fecha=()=>{if(!folha)return;folha.classList.remove('on');setTimeout(()=>folha.open&&folha.close(),320)};
const abre=()=>{
  if(!folha){folha=document.createElement('dialog');folha.className='cs';folha.setAttribute('aria-label','Minha conta');
    folha.innerHTML=`<button class="bar" type="button" aria-label="Fechar"></button><div class="quem"><span class="av">MR</span><div><b>Marina Ribeiro</b><small>marina@exemplo.com</small></div></div>`+
      AREAS.map(([h,n,c,p])=>`<a href="${h}"${h===aqui?' aria-current="page"':''}>${SV(p)}${n}<span class="d">${c?`<em>${c}</em>`:''}${SV('<path d="M9 6l6 6-6 6"/>')}</span></a>`).join('')+
      `<a class="sair" href="login.html">${SV('<path d="M15 12H4M8 8l-4 4 4 4M14 4h5v16h-5"/>')}Sair da conta</a>`;
    document.body.append(folha);
    folha.addEventListener('click',e=>{if(e.target===folha||e.target.closest('.bar'))fecha()});
    folha.addEventListener('cancel',e=>{e.preventDefault();fecha()});
    let y0=null;folha.addEventListener('touchstart',e=>{y0=e.touches[0].clientY},{passive:true});
    folha.addEventListener('touchmove',e=>{if(y0!==null&&e.touches[0].clientY-y0>70){y0=null;fecha()}},{passive:true})}
  folha.showModal();requestAnimationFrame(()=>folha.classList.add('on'))};
$$('.acct').forEach(a=>a.addEventListener('click',e=>{if(!celular.matches)return;e.preventDefault();abre()}));
})();
