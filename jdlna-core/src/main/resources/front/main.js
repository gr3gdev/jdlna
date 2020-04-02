import Header from './modules/header.js';
import Content from './modules/content.js';
import Footer from './modules/footer.js';

class App {
    constructor() {
        this.components = [];
        this.components.push(new Header());
        this.components.push(new Content());
        this.components.push(new Footer());
    }
    render() {
        let parser = new DOMParser();
        document.querySelector('body').innerHTML = '';
        this.components.forEach((elt) => {
            let div = document.createElement('div');
            div.innerHTML = elt.render();
            elt.addReloadEvent(() => {
                div.innerHTML = elt.render();
            });
            document.querySelector('body').appendChild(div);
        });
    }
}

window.addEventListener("DOMContentLoaded", (event) => {
    new App().render();
});