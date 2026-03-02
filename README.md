# ggml

[Roadmap](https://github.com/users/ggerganov/projects/7) / [Manifesto](https://github.com/ggerganov/llama.cpp/discussions/205)

Tensor library for machine learning

***Note that this project is under active development. \
Some of the development is currently happening in the [llama.cpp](https://github.com/ggerganov/llama.cpp) and [whisper.cpp](https://github.com/ggerganov/whisper.cpp) repos***

## Features

- Low-level cross-platform implementation
- Integer quantization support
- Broad hardware support
- Automatic differentiation
- ADAM and L-BFGS optimizers
- No third-party dependencies
- Zero memory allocations during runtime

## Build

```bash
git clone https://github.com/ggml-org/ggml
cd ggml

# install python dependencies in a virtual environment
python3.10 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

# build the examples
mkdir build && cd build
cmake ..
cmake --build . --config Release -j 8
```

## GPT inference (example)

```bash
# run the GPT-2 small 117M model
../examples/gpt-2/download-ggml-model.sh 117M
./bin/gpt-2-backend -m models/gpt-2-117M/ggml-model.bin -p "This is an example"
```

For more information, checkout the corresponding programs in the [examples](examples) folder.

## Using CUDA

```bash
# fix the path to point to your CUDA compiler
cmake -DGGML_CUDA=ON -DCMAKE_CUDA_COMPILER=/usr/local/cuda-12.1/bin/nvcc ..
```

## Using hipBLAS

```bash
cmake -DCMAKE_C_COMPILER="$(hipconfig -l)/clang" -DCMAKE_CXX_COMPILER="$(hipconfig -l)/clang++" -DGGML_HIP=ON
```

## Using SYCL

```bash
# linux
source /opt/intel/oneapi/setvars.sh
cmake -G "Ninja" -DCMAKE_C_COMPILER=icx -DCMAKE_CXX_COMPILER=icpx -DGGML_SYCL=ON ..

# windows
"C:\Program Files (x86)\Intel\oneAPI\setvars.bat"
cmake -G "Ninja" -DCMAKE_C_COMPILER=cl -DCMAKE_CXX_COMPILER=icx -DGGML_SYCL=ON ..
```

## Compiling for Android

Download and unzip the NDK from this download [page](https://developer.android.com/ndk/downloads). Set the NDK_ROOT_PATH environment variable or provide the absolute path to the CMAKE_ANDROID_NDK in the command below.

```bash
cmake .. \
   -DCMAKE_SYSTEM_NAME=Android \
   -DCMAKE_SYSTEM_VERSION=33 \
   -DCMAKE_ANDROID_ARCH_ABI=arm64-v8a \
   -DCMAKE_ANDROID_NDK=$NDK_ROOT_PATH
   -DCMAKE_ANDROID_STL_TYPE=c++_shared
```

```bash
# create directories
adb shell 'mkdir /data/local/tmp/bin'
adb shell 'mkdir /data/local/tmp/models'

# push the compiled binaries to the folder
adb push bin/* /data/local/tmp/bin/

# push the ggml library
adb push src/libggml.so /data/local/tmp/

# push model files
adb push models/gpt-2-117M/ggml-model.bin /data/local/tmp/models/

adb shell
cd /data/local/tmp
export LD_LIBRARY_PATH=/data/local/tmp
./bin/gpt-2-backend -m models/ggml-model.bin -p "this is an example"
```

## Distributed Inference on Android (RPC)

This project supports high-performance distributed inference across multiple Android devices using the GGML RPC backend. One Linux/macOS machine acts as the orchestrator, while multiple Android devices (32-bit or 64-bit) run as RPC servers.

### Key Features
- **Global Socket Persistence**: Eliminates TCP handshake overhead for much faster token generation.
- **32/64-bit Parity**: Standardized 64-bit protocol headers allows a modern PC to work seamlessly with older 32-bit Android hardware.
- **Automatic Discovery**: Devices use UDP pings to find the orchestrator automatically.

### Setup Instructions

#### 1. Build & Install the Android RPC Server
The Android project is located in `llama-rpc-app/`. It supports `arm64-v8a` and `armeabi-v7a` ABIs.
```bash
cd llama-rpc-app
./gradlew assembleDebug
# Install on all targeted Android devices
adb -s <device_id> install app/build/outputs/apk/debug/app-debug.apk
```

#### 2. Build the Linux Orchestrator
Build `llama.cpp` with RPC support enabled:
```bash
# From the project root
cd llama_cpp
mkdir build && cd build
cmake .. -DGGML_RPC=ON
make -j$(nproc) llama-cli
```

#### 3. Run Inference
Use the orchestrator script to automatically detect devices and start inference:
```bash
# In the project root
# Ensure your phones are on the same Wi-Fi as your PC
python3 rpc_inference.py --model path/to/model.gguf --num-devices 2
```

### How it Works
1.  **Discovery**: When you start the service in the Android app, it sends UDP pings to your PC's IP (port 50055).
2.  **Orchestration**: `rpc_inference.py` captures these pings, builds a map of the network, and launches `llama-cli`.
3.  **Workload Splitting**: `llama-cli` splits the model layers across the combined RAM of all connected phones and your PC's CPU/GPU.
4.  **Persistent Communication**: The GGML RPC backend maintains stable TCP connections to each phone, streaming tensor data with minimal latency.

## Resources

- [Introduction to ggml](https://huggingface.co/blog/introduction-to-ggml)
- [The GGUF file format](https://github.com/ggerganov/ggml/blob/master/docs/gguf.md)
