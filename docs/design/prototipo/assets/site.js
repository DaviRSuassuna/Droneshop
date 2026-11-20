/* Interações do protótipo da loja */
(function(){
  const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];
  const m=$('#menuBtn');if(m)m.addEventListener('click',()=>{const o=$('#links').classList.toggle('open');m.setAttribute('aria-expanded',o)});
  const fb=$('#filtBtn');if(fb)fb.addEventListener('click',()=>{const o=$('.filters').classList.toggle('open');fb.setAttribute('aria-expanded',o)});
  // etiquetas de filtro: o "×" tira o filtro; "Limpar" tira todos e desmarca o painel
  document.addEventListener('click',e=>{
    const x=e.target.closest('.tag button');if(x){e.preventDefault();x.closest('.tag').remove();return}
    const l=e.target.closest('a');if(l&&/^\s*Limpar\s*$/.test(l.textContent)){e.preventDefault();
      $$('.tag').forEach(t=>{if(t.querySelector('button'))t.remove()});
      $$('.filters input[type=checkbox],.filters input[type=radio]').forEach(i=>i.checked=false)}});
  // menu da conta no celular: a aba atual fica visível
  // quantidade
  $$('.qty').forEach(q=>{const o=$('output',q),[a,b]=$$('button',q);
    a.onclick=()=>{o.value=Math.max(1,+o.value-1);q.dispatchEvent(new Event('change',{bubbles:true}))};
    b.onclick=()=>{o.value=+o.value+1;q.dispatchEvent(new Event('change',{bubbles:true}))}});
  // grupos de abas genéricos: [data-tabs] com botões [data-t] e painéis [data-p]
  $$('[data-tabs]').forEach(g=>{const bs=$$('[data-t]',g);bs.forEach(b=>b.addEventListener('click',()=>{
    bs.forEach(x=>x.setAttribute('aria-selected',x===b));
    $$('[data-p]',g).forEach(p=>p.hidden=p.dataset.p!==b.dataset.t)}))});
  // rádios que mostram painéis: input[data-show]
  $$('input[data-show]').forEach(r=>r.addEventListener('change',()=>{
    $$(`input[name="${r.name}"]`).forEach(x=>{const p=document.getElementById(x.dataset.show);if(p)p.hidden=!x.checked})}));
  // galeria
  $$('[data-thumb]').forEach(t=>t.addEventListener('click',()=>{
    $$('[data-thumb]').forEach(x=>x.setAttribute('aria-pressed',x===t));
    const main=$('#gmain'),tp=t.querySelector('template.tsrc');main.innerHTML=tp?tp.innerHTML:''}));
  // versão do produto
  $$('[data-ver]').forEach(v=>v.addEventListener('change',()=>{$('#pprice').textContent=v.dataset.ver;$('#pinst').textContent=v.dataset.inst}));
  // remover item / favorito
  $$('[data-remove]').forEach(b=>b.addEventListener('click',e=>{e.preventDefault();const el=b.closest(b.dataset.remove);el.style.opacity=.35;el.style.pointerEvents='none'}));
  // copiar
  $$('[data-copy]').forEach(b=>b.addEventListener('click',()=>{const t=b.dataset.copy,done=()=>{const o=b.textContent;b.textContent='Copiado';setTimeout(()=>b.textContent=o,1600)};
    try{navigator.clipboard.writeText(t).then(done,done)}catch(e){done()}}));
  // formulários do protótipo não enviam
  $$('form').forEach(f=>f.addEventListener('submit',e=>{e.preventDefault();const b=$('[type=submit]',f);if(b){const o=b.textContent;b.textContent='Salvo';setTimeout(()=>b.textContent=o,1600)}}));
})();

(function(){var b=document.getElementById('searchBtn'),m=document.getElementById('msearch');
 if(b&&m)b.addEventListener('click',function(){var o=m.classList.toggle('open');b.setAttribute('aria-expanded',o);var l=document.getElementById('links');if(o&&l){l.classList.remove('open')}if(o)m.querySelector('input').focus()});
 document.addEventListener('keydown',function(e){if(e.key==='/'&&!/input|textarea|select/i.test(document.activeElement.tagName)){var i=document.querySelector('.search input');if(i&&i.offsetParent){e.preventDefault();i.focus()}}});
 document.querySelectorAll('form.msearch,.search').forEach(function(f){f.addEventListener('submit',function(e){e.preventDefault();location.href='drones.html'})});
})();

document.querySelectorAll('[data-toggle]').forEach(function(b){b.addEventListener('click',function(){var el=document.getElementById(b.dataset.toggle);if(!el)return;el.hidden=!el.hidden;document.querySelectorAll('[data-toggle="'+b.dataset.toggle+'"][aria-expanded]').forEach(function(x){x.setAttribute('aria-expanded',!el.hidden)});if(!el.hidden){el.scrollIntoView({behavior:'smooth',block:'start'});var f=el.querySelector('input:not([type=radio]),textarea');if(f)setTimeout(function(){f.focus()},300)}})});

/* pop-ups: [data-dialog] abre, [data-close] e clique fora fecham */
document.querySelectorAll('[data-dialog]').forEach(function(b){b.addEventListener('click',function(){var d=document.getElementById(b.dataset.dialog);if(!d)return;d.showModal();var f=d.querySelector('input');if(f)f.focus()})});
document.querySelectorAll('dialog.dlg').forEach(function(d){
  d.addEventListener('click',function(e){if(e.target===d)d.close()});
  d.querySelectorAll('[data-close]').forEach(function(c){c.addEventListener('click',function(){d.close()})});
  d.addEventListener('close',function(){var f=d.querySelector('form');if(f){f.reset();d.querySelectorAll('.rules li').forEach(function(l){l.classList.remove('ok')});var m=d.querySelector('.pwmsg');if(m){m.textContent='';m.className='pwmsg'}}});
});
document.querySelectorAll('[data-eye]').forEach(function(b){b.addEventListener('click',function(){var i=b.parentNode.querySelector('input'),s=i.type==='password';i.type=s?'text':'password';b.setAttribute('aria-pressed',s);b.setAttribute('aria-label',s?'Ocultar senha':'Mostrar senha')})});
document.querySelectorAll('form[data-pwform]').forEach(function(f){
  var n=f.querySelector('#s2'),c=f.querySelector('#s3'),a=f.querySelector('#s1'),m=f.querySelector('#pwMsg');
  function rules(){var v=n.value;var t={len:v.length>=8,let:/[a-zA-Z]/.test(v),num:/\d/.test(v)};f.querySelectorAll('.rules li').forEach(function(l){l.classList.toggle('ok',t[l.dataset.r])});return t.len&&t.let&&t.num}
  n.addEventListener('input',rules);
  f.addEventListener('submit',function(e){e.preventDefault();e.stopImmediatePropagation();
    if(!a.value){m.className='pwmsg err';m.textContent='Informe a senha atual.';a.focus();return}
    if(!rules()){m.className='pwmsg err';m.textContent='A nova senha não atende aos requisitos.';n.focus();return}
    if(n.value!==c.value){m.className='pwmsg err';m.textContent='A confirmação não confere com a nova senha.';c.focus();return}
    m.className='pwmsg good';m.textContent='Senha atualizada.';setTimeout(function(){f.closest('dialog').close()},1100)},true);
});

/* formulários em pop-up: valida obrigatórios, confirma e fecha */
document.querySelectorAll('form[data-dlgform]').forEach(function(f){
  var m=f.querySelector('.pwmsg'),b=f.querySelector('[type=submit]'),orig=b.textContent;
  f.addEventListener('submit',function(e){e.preventDefault();e.stopImmediatePropagation();
    var vazio=[].filter.call(f.querySelectorAll('.f'),function(x){var i=x.querySelector('input:not([type=radio]):not([type=file]),select,textarea');return x.querySelector('.req')&&i&&!i.value.trim()});
    f.querySelectorAll('.f input,.f textarea').forEach(function(i){i.style.borderColor=''});
    if(vazio.length){vazio.forEach(function(x){x.querySelector('input:not([type=radio]):not([type=file]),select,textarea').style.borderColor='var(--warn)'});m.className='pwmsg err';m.textContent=vazio.length===1?'Preencha o campo obrigatório destacado.':'Preencha os '+vazio.length+' campos obrigatórios destacados.';vazio[0].querySelector('input:not([type=radio]):not([type=file]),select,textarea').focus();return}
    m.className='pwmsg good';m.textContent=f.dataset.ok||'Salvo.';b.disabled=true;setTimeout(function(){b.disabled=false;f.closest('dialog').close()},f.dataset.ok?1800:900)},true);
  f.closest('dialog').addEventListener('close',function(){f.querySelectorAll('.f input,.f textarea').forEach(function(i){i.style.borderColor=''})});
});
/* prévia do cartão */
(function(){var n=document.getElementById('cn');if(!n||!document.getElementById('dlgCartao'))return;
 var g=function(i){return document.getElementById(i)};
 n.addEventListener('input',function(){var d=n.value.replace(/\D/g,'').slice(0,16);n.value=d.replace(/(\d{4})(?=\d)/g,'$1 ');var p=(d+'••••••••••••••••').slice(0,16);g('ppNum').textContent=p.replace(/(.{4})/g,'$1 ').trim();g('ppBrand').textContent=/^4/.test(d)?'VISA':/^5[1-5]/.test(d)?'MASTERCARD':/^3[47]/.test(d)?'AMEX':'CARTÃO'});
 g('cnm').addEventListener('input',function(){g('ppName').textContent=(this.value||'Nome impresso').toUpperCase()});
 g('cv').addEventListener('input',function(){var d=this.value.replace(/\D/g,'').slice(0,4);this.value=d.length>2?d.slice(0,2)+'/'+d.slice(2):d;g('ppVal').textContent=this.value||'MM/AA'});
 g('dlgCartao').addEventListener('close',function(){g('ppNum').textContent='•••• •••• •••• ••••';g('ppName').textContent='NOME IMPRESSO';g('ppVal').textContent='MM/AA';g('ppBrand').textContent='CARTÃO'});
})();
