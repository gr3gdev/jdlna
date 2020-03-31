import Component from './component.js';
import Rest from './rest-api.js';

class Content extends Component {
    constructor() {
        super();
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
        new Rest().getConfiguration(function(res) {
            this.state = JSON.parse(res);
            update();
        });
    }
    save() {
        console.log('Save...');
    }
    render() {
        return `<div class="ui main text container">
                    <h1 class="ui header">Configuration</h1>
                    <div class="ui container">
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="film icon"></i>
                                Vid&eacute;os
                            </a>
                            <input type="text" name="folder.video" value="${this.state.video.path}">
                            <div class="ui basic label">${this.state.video.count}</div>
                        </div>
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="music icon"></i>
                                Musiques
                            </a>
                            <input type="text" name="folder.music" value="${this.state.music.path}">
                            <div class="ui basic label">${this.state.music.count}</div>
                        </div>
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="photo icon"></i>
                                Images
                            </a>
                            <input type="text" name="folder.photo" value="${this.state.photo.path}">
                            <div class="ui basic label">${this.state.photo.count}</div>
                        </div>
                        <div class="ui right labeled input container margin-line">
                            <a class="ui label icon size-8">
                                <i class="book icon"></i>
                                Covers
                            </a>
                            <input type="text" name="folder.cover" value="${this.state.cover.path}">
                            <div class="ui basic label">${this.state.cover.count}</div>
                        </div>
                    </div>
                    <button class="ui primary button" tabindex="0" onclick="document.componentRegistry[${this._id}].save()">Sauvegarder</button>
                    <button class="ui button">Recharger les fichiers</button>
                </div>`;
    }
}

export default Content;