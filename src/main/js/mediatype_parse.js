var digitToWord = {
    "1": "one",
    "2": "two",
    "3": "three",
    "4": "four",
    "5": "five",
    "6": "six",
    "7": "seven",
    "8": "eight",
    "9": "nine",
    "0": "zero"
};

var rows = document.querySelectorAll('#table-application tbody tr');
var result = '';
// Проходимся по каждой строке
rows.forEach(function (row) {
    var nameCell = row.cells[0];
    var linkCell = row.cells[2];

    // Получаем текст из ячеек
    var name = nameCell.textContent.trim().replace(/[-+.]/g, '_').replace(/^\d/, match => digitToWord[match] + '_');
    var temp = name.split(" ");
    name = temp[0];
    var link = linkCell.innerHTML.trim();
    link = link.slice(1, -1);
    link = link.replace(/"#/g, 'https://www.iana.org/assignments/media-types/media-types.xhtml#');

    result += '/** DOC:';

    result += link + ' ' + name

    if (temp.length > 2) {
        for (var i = 1; i < temp.length; i++) {
            result += temp[i] + ' ';
        }
    }
    result += '*/' + '\n';
    result += name + ',' + '\n';
});
result = result.slice(0, -2) + ';';
console.log(result);
