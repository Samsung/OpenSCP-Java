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


class Const:
    class Path:
        # root dirs
        PROJECT_ROOT = pathlib.Path(__file__).parent.parent
        # java dirs
        JAVA_BUILD_DIR = PROJECT_ROOT / "build"
        JAVA_LIBS_DIR = JAVA_BUILD_DIR / "libs"
        JAVA_DOCS_DIR = JAVA_BUILD_DIR / "docs" / "javadoc"
        # artifacts
        OUT_DIR = PROJECT_ROOT / ".out"
        OUT_LIB_DIR = OUT_DIR / "lib"
        OUT_DOCS_DIR = OUT_DIR / "javadoc"
