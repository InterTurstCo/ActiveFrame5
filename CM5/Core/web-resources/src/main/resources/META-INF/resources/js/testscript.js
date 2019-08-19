/**
 * Created by Ravil on 26.07.2019.
 */
(function () {
    var checkbox = document.querySelector('#active input');
    var yearbox = document.querySelector('#year');

    if(checkbox.checked) {
        yearbox.style.display='';
    } else {
        yearbox.style.display='none';
    }

    checkbox.addEventListener('change', function () {
        if (this.checked) {
            yearbox.style.display='';
        } else {
            yearbox.value = '*';
            yearbox.style.display='none';
        }
    });
})();