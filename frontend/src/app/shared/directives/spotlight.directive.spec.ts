import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SpotlightDirective } from './spotlight.directive';

@Component({
  imports: [SpotlightDirective],
  template: `<section
    appSpotlight
    style="position:absolute;left:0;top:0;width:200px;height:100px"
  ></section>`,
})
class Hospede {}

describe('SpotlightDirective', () => {
  it('leva a luz para onde o mouse está', () => {
    const f = TestBed.createComponent(Hospede);
    f.detectChanges();
    const el: HTMLElement = f.nativeElement.querySelector('section');
    const r = el.getBoundingClientRect();
    el.dispatchEvent(
      new PointerEvent('pointermove', { clientX: r.left + 30, clientY: r.top + 40 }),
    );
    expect(el.style.getPropertyValue('--x')).toBe('30px');
    expect(el.style.getPropertyValue('--y')).toBe('40px');
  });
});
