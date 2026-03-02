# Capstone Project: Distributed Inference on Android (RPC)

This project implements a high-performance system for distributed LLM inference across multiple Android devices using the GGML RPC backend.

## Key Features
- **Global Socket Persistence**: Eliminates TCP handshake overhead for much faster token generation.
- **32/64-bit Parity**: Standardized 64-bit protocol headers allowing a modern PC to work seamlessly with older 32-bit Android hardware.
- **Automatic Discovery**: Devices use UDP pings to find the orchestrator automatically.

---

## Setup & Maintenance

### 1. Submodule Management
The system relies on a customized version of `llama.cpp` tracked as a git submodule in the `llama_cpp/` directory.

**Fresh Clone:**
If you just cloned this repository, initialize the submodule:
```bash
git submodule update --init --recursive
```

**Updating:**
To pull the latest changes from the submodule's remote:
```bash
git submodule update --remote --merge
```

### 2. Build & Install Android RPC Server
The Android app is located in `llama-rpc-app/`. It supports both `arm64-v8a` and `armeabi-v7a` architectures.

```bash
cd llama-rpc-app
./gradlew assembleDebug
# Install on devices
adb -s <device_id> install app/build/outputs/apk/debug/app-debug.apk
```
*Note: Open the app on each device and tap **Start RPC Server**.*

### 3. Build Linux Orchestrator
Build the `llama.cpp` CLI with RPC support:
```bash
cd llama_cpp
mkdir build && cd build
cmake .. -DGGML_RPC=ON
make -j$(nproc) llama-cli
```

### 4. Run Distributed Inference
Ensure all devices are on the same Wi-Fi network as your PC:
```bash
# From the project root
python3 rpc_inference.py --model path/to/model.gguf --num-devices 2
```

---

## How it Works

1.  **Discovery**: The Android app broadcasts UDP pings to port 50055.
2.  **Orchestration**: `rpc_inference.py` captures pings, builds a network map, and launches `llama-cli`.
3.  **Workload Splitting**: `llama-cli` splits the model layers across the available RAM of all connected devices and the orchestrator's CPU/GPU.
4.  **Persistent Communication**: The GGML RPC backend maintains stable TCP connections to each phone, streaming tensor data with minimal latency.
