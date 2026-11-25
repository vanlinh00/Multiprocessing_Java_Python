# Multiprocessing (`ProcessBuilder`)

One program starts and controls another child process directly inside its code (for example, Java launching a Python script).

## 1. What is the problem?
When one program tries to do too much heavy work in one single space, things can slow down or break:
- **One crash can ruin everything:** If heavy code runs out of memory (`OutOfMemoryError`) or crashes inside your main app, the whole program can go down.
- **Heavy tasks block speed:** Big jobs (like processing large files or running outside scripts) can slow down normal work.

## 2. Why use multiprocessing?
Multiprocessing runs tasks in **separate OS processes** with **separate memory**.
- **Safety:** If a child process crashes, your main app can stay alive.
- **Outside tools:** You can easily run other programs or scripts (like Python or `ffmpeg`) from Java.
- **More CPU power:** Multiple processes can run on multiple CPU cores at the same time.