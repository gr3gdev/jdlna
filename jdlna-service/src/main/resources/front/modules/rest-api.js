class Rest {
    constructor() {
        this.xhr = new XMLHttpRequest();
        this.actions = {
            success: function(res) {
                console.log('success!', res);
            },
            error: function() {
                console.log('The request failed!');
            }
        }
        this.xhr.onload = function() {
            if (xhr.status >= 200 && xhr.status < 300) {
                // What do when the request is successful
                this.actions.success(xhr.response);
            } else {
                // What do when the request fails
                this.actions.error();
            }
        };
    }
    getConfiguration(success, error) {
        if (success) {
            this.actions.success = success;
        }
        if (error) {
            this.actions.error = error;
        }
        this.xhr.open('GET', 'http://localhost:9300/jdlna/api/latest/conf');
        this.xhr.send();
    }
}

export default Rest;