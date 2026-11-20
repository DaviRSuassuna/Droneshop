import { defineConfig, devices } from '@playwright/test';

/**
 * Testes de conformidade das telas com o protótipo (e2e/conformidade).
 *
 * Sobe o Angular (porta 4200) e o protótipo (porta 4300, a mesma pasta
 * docs/design/prototipo que o GitHub Pages publica). Para comparar com a versão
 * já publicada no Pages: PROTOTIPO_URL=https://davirsuassuna.github.io/droneshop/prototipo/
 * A API não precisa estar de pé: as respostas são simuladas (e2e/conformidade/api-falsa.ts).
 */
// Portas por variável para as duas trilhas rodarem ao mesmo tempo (docs/agentes/README.md):
// trilha B usa PORTA_APP=4211 e PORTA_PROTOTIPO=4311.
const portaApp = process.env['PORTA_APP'] ?? '4200';
const portaPrototipo = process.env['PORTA_PROTOTIPO'] ?? '4300';
const prototipoUrl = process.env['PROTOTIPO_URL'] ?? `http://localhost:${portaPrototipo}/`;

export default defineConfig({
  testDir: './e2e',
  outputDir: './e2e/.resultados',
  timeout: 60_000,
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: 0,
  reporter: [['list'], ['html', { outputFolder: 'e2e/.relatorio', open: 'never' }]],
  use: {
    baseURL: `http://localhost:${portaApp}`,
    viewport: { width: 1280, height: 800 },
    locale: 'pt-BR',
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 800 },
        launchOptions: process.env['CHROMIUM_PATH'] ? { executablePath: process.env['CHROMIUM_PATH'] } : {},
      },
    },
  ],
  metadata: { prototipoUrl },
  webServer: [
    {
      command: `npx ng serve --port ${portaApp}`,
      url: `http://localhost:${portaApp}`,
      reuseExistingServer: !process.env['CI'],
      timeout: 180_000,
    },
    ...(process.env['PROTOTIPO_URL']
      ? []
      : [
          {
            command: 'node scripts/servir-prototipo.mjs',
            url: `http://localhost:${portaPrototipo}/`,
            reuseExistingServer: !process.env['CI'],
            env: { PORTA: portaPrototipo },
          },
        ]),
  ],
});
