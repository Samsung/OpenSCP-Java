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
import argparse
import enum
import sys

from constants import Const
from utils import collect_artifacts, run_cmd, run_cmds


class Component(str, enum.Enum):
    LIB = "lib"
    DOC = "doc"


def main() -> None:
    options = parse_arguments()
    match options.component:
        case Component.LIB.value:
            build_java(options.clean_build)
        case Component.DOC.value:
            build_javadoc()
        case _:
            sys.exit(f"'{options.component}' component build is unsupported")


def parse_arguments() -> argparse.Namespace:
    components = [component.value for component in Component]
    parser = argparse.ArgumentParser("Build Java SCP library sources and generate documentation",
                                     formatter_class=argparse.RawTextHelpFormatter)
    parser.add_argument("-c", "--component",
                        choices=components,
                        required=True,
                        help="Target component to build:\n"
                             f"  {Component.LIB.value}: Java SCP library\n"
                             f"  {Component.DOC.value}: Java SCP library documentation")
    parser.add_argument("-cb", "--clean-build",
                        action="store_true",
                        help="Perform project clean before build")
    options = parser.parse_args()
    return options


def build_java(clean_build: bool) -> None:
    remove_java_artifacts([
        Const.Path.JAVA_LIBS_DIR,
        Const.Path.JAVA_DOCS_DIR,
        Const.Path.DELIVERABLES_JAVA_DIR])
    gradle_project_build(clean_build)
    collect_java_artifacts()


def build_javadoc() -> None:
    remove_java_artifacts([Const.Path.DELIVERABLES_JAVA_DOC_DIR])
    gradle_project_doc_build()
    collect_java_doc_artifacts()


def remove_java_artifacts(dirs_to_remove: list[pathlib.Path]) -> None:
    commands = [f"rm -rf {dir_path}" for dir_path in dirs_to_remove]
    run_cmds(commands)


def gradle_project_build(clean_build: bool) -> None:
    commands = []
    if clean_build:
        commands.append(build_gradle_cmd("clean"))
    commands.append(build_gradle_cmd("build"))
    run_cmds(commands)


def gradle_project_doc_build() -> None:
    run_cmd(build_gradle_cmd("javadoc"))


def build_gradle_cmd(action: str) -> str:
    cmd_template = "{project_path}/gradlew --console=plain -p {project_path} :{action}"
    return cmd_template.format(project_path=Const.Path.PROJECT_ROOT, action=action)


def collect_java_artifacts() -> None:
    lib_artifacts_path = Const.Path.JAVA_LIBS_DIR / "*"
    collect_artifacts(lib_artifacts_path, Const.Path.DELIVERABLES_JAVA_DIR)


def collect_java_doc_artifacts() -> None:
    doc_artifacts_path = Const.Path.JAVA_DOCS_DIR / "*"
    collect_artifacts(doc_artifacts_path, Const.Path.DELIVERABLES_JAVA_DIR)


if __name__ == "__main__":
    main()
