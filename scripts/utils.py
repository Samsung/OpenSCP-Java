__copyright__ = """
Copyright 2025 Samsung Electronics Co, Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import pathlib
import subprocess
import sys


def collect_artifacts(artifacts_path: pathlib.Path, destination_dir: pathlib.Path) -> None:
    commands = [f"mkdir -p {destination_dir}",
                f"cp -R {artifacts_path} {destination_dir}"]
    run_cmds(commands)


def run_cmds(commands: list[str] | list[list[str]]) -> None:
    for command in commands:
        run_cmd(command)


def run_cmd(cmd: str | list[str], capture_stdout: bool = False) -> subprocess.CompletedProcess[bytes]:
    print(f"Command: {cmd}")
    stdout = subprocess.PIPE if capture_stdout else None
    process = subprocess.run(cmd,
                             shell=True,
                             stdout=stdout,
                             stderr=subprocess.PIPE,
                             check=False)
    stderr = process.stderr.decode()
    if stderr:
        print("Stderr:\n" + stderr)
    if process.returncode != 0:
        if capture_stdout:
            print("Stdout:\n" + process.stdout.decode())
        sys.exit("Command failed!")
    return process
