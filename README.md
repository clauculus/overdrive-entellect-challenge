# stima-overdrive
Implementasi algoritma Greedy pada bot Overdrive Entelect Challenge 2020

## Requirement
1. Java (minimal Java 8): https://www.oracle.com/java/technologies/javase/javasejdk8-downloads.html
2. IntelIJ IDEA: https://www.jetbrains.com/idea/
3. NodeJS: https://nodejs.org/en/download/

## How to run
Untuk menjalankan tanpa mengubah bot lawan, lakukan langkah-langkah berikut:
1. Clone atau download as zip <a href="https://github.com/clauculus/stima-overdrive">repository berikut</a>.
2. Untuk sistem operasi Windows, <i>double click</i> run.bat atau buka command prompt, masuk ke folder root tempat Anda meng-<i>clone/download</i> kode sumber, lalu jalankan

`run.bat`

Untuk menjalankan dengan mengubah bot lawan, lakukan langkah-langkah berikut:
1. Masuk ke folder <i>reference-bot > java > target</i> lalu masukkan file .jar bot lawan
2. Masuk ke folder <i>reference-bot > java</i> lalu buka dan edit file bot.json pada bagian <i>namaBotLawan</i>:

`"botFileName": "namaBotLawan.jar",`

3. Ubah bagian <i>player-a</i> dan <i>player-b</i> pada game-runner-config.json berikut:

`"player-a": "./starter-bots/java",`
`"player-b": "./starter-bots/java",`

## Author

<table>
<tr><td>No.</td><td>Nama</td><td>NIM</td></tr>
<tr><td>1.</td><td>Maharani Ayu Putri Irawan</td><td>13520019</td></tr>
<tr><td>2.</td><td>Lyora Felicya</td><td>13520073</td></tr>
<tr><td>3.</td><td>Claudia</td><td>13520076</td></tr>
