import os
import subprocess
import tempfile
import pytest

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

BIN_COPY = os.path.join(BASE_DIR, "copy")
BIN_FIND = os.path.join(BASE_DIR, "find")
BIN_LIST = os.path.join(BASE_DIR, "list")
BIN_PS = os.path.join(BASE_DIR, "process_status")

def test_copy():
    with tempfile.TemporaryDirectory() as tmpdir:
        src = os.path.join(tmpdir, "archivo.txt")
        dst = os.path.join(tmpdir, "copia.txt")
        with open(src, "w") as f:
            f.write("Hola mundo!\n")
        res = subprocess.run([BIN_COPY, src, dst], capture_output=True)
        assert res.returncode == 0
        assert os.path.exists(dst)
        with open(dst) as f:
            assert f.read() == "Hola mundo!\n"
        res2 = subprocess.run([BIN_COPY, src, dst], capture_output=True)
        assert res2.returncode != 0

def test_find():
    with tempfile.TemporaryDirectory() as tmpdir:
        filename = "archivo_xyz.txt"
        with open(os.path.join(tmpdir, filename), "w"):
            pass
        res = subprocess.run([BIN_FIND, "xyz"], cwd=tmpdir, capture_output=True, text=True)
        assert filename in res.stdout

def test_list():
    with tempfile.TemporaryDirectory() as tmpdir:
        file_path = os.path.join(tmpdir, "archivo.txt")
        with open(file_path, "w") as fl:
            fl.write("hola")
        res = subprocess.run([BIN_LIST, tmpdir], capture_output=True, text=True)
        assert "archivo.txt" in res.stdout
        assert "-" in res.stdout

def test_process_status():
    res = subprocess.run([BIN_PS], capture_output=True, text=True)
    assert "process_status" in res.stdout or "ps0" in res.stdout or "bash" in res.stdout or "python" in res.stdout

if __name__ == "__main__":
    pytest.main([__file__])
