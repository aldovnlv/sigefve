import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/*
  Configuración para Vite.

  PARA SUBIR A NGINX:
  1) Ejecutar en tu máquina:
       npm install
       npm run build

  2) La carpeta "dist" generada es la que se debe copiar al servidor
     y usar como root en la configuración de Nginx, por ejemplo:

       server {
           listen 80;
           server_name midominio.com;

           root /var/www/sigefve/dist;
           index index.html;

           location / {
               try_files $uri /index.html;
           }
       }

  3) Si la app se servirá en un subdirectorio (por ejemplo /sigefve/),
     puedes agregar base en esta configuración:

       export default defineConfig({
         base: '/sigefve/',
         plugins: [react()]
       });

     y volver a ejecutar "npm run build".
*/

export default defineConfig({
  plugins: [react()]
});
