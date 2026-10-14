(function(){
  var K='aeria-theme';
  var t=localStorage.getItem(K)||'light';
  document.documentElement.setAttribute('data-theme',t);
  function label(){return document.documentElement.getAttribute('data-theme')==='light'?'\u25D1 Tema claro':'\u25D0 Tema escuro';}
  document.addEventListener('DOMContentLoaded',function(){
    var b=document.createElement('button');
    b.className='theme-t';b.type='button';b.textContent=label();
    b.addEventListener('click',function(){
      var n=document.documentElement.getAttribute('data-theme')==='light'?'dark':'light';
      document.documentElement.setAttribute('data-theme',n);
      localStorage.setItem(K,n);b.textContent=label();
    });
    document.body.appendChild(b);
  });
})();