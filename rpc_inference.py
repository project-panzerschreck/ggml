#!/usr/bin/env python3
import argparse
import subprocess
import os

def main():
    parser = argparse.ArgumentParser(description="Distributed Inference Orchestrator")
    parser.add_argument("--model", required=True, help="Path to GGUF model")
    parser.add_argument("--rpc", required=True, help="Comma-separated list of phone_ip:port (e.g., 192.168.1.10:50052,192.168.1.11:50052)")
    parser.add_argument("--prompt", default="Explain quantum entanglement in simple terms.", help="Prompt for inference")
    parser.add_argument("--threads", type=int, default=8, help="Number of local threads")
    
    args = parser.parse_args()
    
    llama_cli_path = "/home/angela/Code/ggml/llama_cpp/build-host/bin/llama-cli"
    if not os.path.exists(llama_cli_path):
        # Fallback to just llama-cli in PATH
        llama_cli_path = "llama-cli"

    cmd = [
        llama_cli_path,
        "-m", args.model,
        "-p", args.prompt,
        "-t", str(args.threads),
        "--rpc", args.rpc
    ]
    
    print(f"Running distributed inference command:\n{' '.join(cmd)}")
    
    try:
        subprocess.run(cmd, check=True)
    except FileNotFoundError:
        print(f"Error: {llama_cli_path} not found. Please build llama.cpp first.")
    except subprocess.CalledProcessError as e:
        print(f"Inference failed with error code: {e.returncode}")

if __name__ == "__main__":
    main()
