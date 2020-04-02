class Rest {
    constructor() {
        this.xhr = new XMLHttpRequest();
        this.url = window.location.href;
        if (this.url.charAt(this.url.length - 1) !== '/') {
            this.url += '/';
        }
        this.actions = {
            success: function(res) {
                console.log('success!', res);
            },
            error: function() {
                console.log('The request failed!');
            }
        }
        this.xhr.addEventListener('load', (evt) => {
            let res = evt.target;
            if (res.status >= 200 && res.status < 300) {
                // What do when the request is successful
                this.actions.success(res.response);
            } else {
                // What do when the request fails
                this.actions.error(res.responseText);
            }
        });
    }
    save(data, success, error) {
        if (success) {
            this.actions.success = success;
        }
        if (error) {
            this.actions.error = error;
        }
        this.xhr.open('POST', `${this.url}api/latest/save`, true);
        this.xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        this.xhr.send(data);
    }
    reload(success, error) {
        if (success) {
            this.actions.success = success;
        }
        if (error) {
            this.actions.error = error;
        }
        this.xhr.open('POST', `${this.url}api/latest/reload`, true);
        this.xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        this.xhr.send();
    }
    getConfiguration(success, error) {
        if (success) {
            this.actions.success = success;
        }
        if (error) {
            this.actions.error = error;
        }
        this.xhr.open('GET', `${this.url}api/latest/conf`, true);
        this.xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        this.xhr.send();
    }
}

export default Rest;