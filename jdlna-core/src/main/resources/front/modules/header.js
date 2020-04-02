import Component from './component.js';

class Header extends Component {
    render() {
        return `<div class="ui menu">
                    <div class="ui container">
                        <a class="header item" href="#">
                            <img class="logo" src="images/logo.png">
                            jDLNA
                        </a>
                    </div>
                </div>`;
    }
}

export default Header;