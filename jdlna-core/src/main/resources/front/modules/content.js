import Component from './component.js';
import Rest from './rest-api.js';

class Content extends Component {
    constructor() {
        super();
        this.rest = new Rest();
        this.messages = [];
        this.error = null;
        this.state = {
            video: {
                path: '/media/videos',
                count: 0
            },
            music: {
                path: '/media/musics',
                count: 0
            },
            photo: {
                path: '/media/images',
                count: 0
            },
            cover: {
                path: '/media/covers',
                count: 0
            }
        }
        this.loadConf();
    }
    loadConf() {
        this.rest.getConfiguration((res) => {
            if (res) {
                this.state = JSON.parse(res);
                super.update();
            }
        });
    }
    handleChange(elt) {
        if (elt.name === 'folder.video') {
            this.state.video.path = elt.value;
        }
        if (elt.name === 'folder.music') {
            this.state.music.path = elt.value;
        }
        if (elt.name === 'folder.photo') {
            this.state.photo.path = elt.value;
        }
        if (elt.name === 'folder.cover') {
            this.state.cover.path = elt.value;
        }
    }
    addMessage(msg) {
        // MAX 3 messages
        if (this.messages.length === 3) {
            this.messages.splice(0, 1);
        }
        this.messages.push(msg);
    }
    reloadFiles() {
        this.error = null;
        this.addMessage('Chargement en cours...');
        super.update();
        this.rest.reload((res) => {
            if (res) {
                this.addMessage(JSON.parse(res).status);
                this.loadConf();
            }
        }, (err) => {
            this.error = err;
        });
    }
    save() {
        this.error = null;
        this.addMessage('Sauvegarde en cours...');
        super.update();
        this.rest.save(JSON.stringify(this.state), (res) => {
            if (res) {
                this.addMessage(JSON.parse(res).status);
                this.loadConf();
            }
        }, (err) => {
            this.error = err;
        });
    }
    closeMessage(index) {
        this.messages.splice(index, 1);
        super.update();
    }
    closeError() {
        this.error = null;
        super.update();
    }
    render() {
        let htmlMessage = '';
        let index = 0;
        this.messages.forEach((msg) => {
            htmlMessage += `<div class="ui info message">
                                <i class="close icon" onclick="document.componentRegistry[${this.id}].closeMessage(${index})"></i>
                                ${msg}
                            </div>`;
            index++;
        });
        let htmlError = `<div class="ui negative message">
                            <i class="close icon" onclick="document.componentRegistry[${this.id}].closeError()"></i>
                            ${this.error}
                        </div>`;
        return `<div class="ui main text container">
                    <h1 class="ui header">Configuration</h1>
                    ${(this.error) ? htmlError : ''}
                    ${(this.messages.length > 0) ? htmlMessage : ''}
                    <div class="ui container">
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="film icon"></i>
                                Vid&eacute;os
                            </a>
                            <input type="text" name="folder.video" onchange="document.componentRegistry[${this.id}].handleChange(this)" value="${this.state.video.path}">
                            <div class="ui basic label">${this.state.video.count}</div>
                        </div>
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="music icon"></i>
                                Musiques
                            </a>
                            <input type="text" name="folder.music" onchange="document.componentRegistry[${this.id}].handleChange(this)" value="${this.state.music.path}">
                            <div class="ui basic label">${this.state.music.count}</div>
                        </div>
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="photo icon"></i>
                                Images
                            </a>
                            <input type="text" name="folder.photo" onchange="document.componentRegistry[${this.id}].handleChange(this)" value="${this.state.photo.path}">
                            <div class="ui basic label">${this.state.photo.count}</div>
                        </div>
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="book icon"></i>
                                Covers
                            </a>
                            <input type="text" name="folder.cover" onchange="document.componentRegistry[${this.id}].handleChange(this)" value="${this.state.cover.path}">
                            <div class="ui basic label">${this.state.cover.count}</div>
                        </div>
                    </div>
                    <div class="ui actions">
                        <button class="ui primary button" tabindex="0" onclick="document.componentRegistry[${this.id}].save()">Sauvegarder</button>
                        <button class="ui button" onclick="document.componentRegistry[${this.id}].reloadFiles()">Recharger les fichiers</button>
                    </div>
                </div>`;
    }
}

export default Content;