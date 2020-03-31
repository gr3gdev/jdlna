import Header from './modules/header.js';
import Content from './modules/content.js';
import Footer from './modules/footer.js';

let header = new Header();
let content = new Content();
let footer = new Footer();

window.addEventListener("DOMContentLoaded", (event) => {
    document.querySelector('body').innerHTML = header.render()
        + content.render()
        + footer.render();
});