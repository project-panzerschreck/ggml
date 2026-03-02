#!/usr/bin/env python3
import argparse
import subprocess
import os

def main():
    parser = argparse.ArgumentParser(description="Distributed Inference Orchestrator")
    parser.add_argument("--model", required=True, help="Path to GGUF model")
    parser.add_argument("--rpc", required=False, help="Comma-separated list of phone_ip:port. If omitted, will discover devices locally via UDP.")
    parser.add_argument("--num-devices", type=int, default=2, help="Number of devices to wait for during discovery if --rpc is omitted")
    parser.add_argument("--prompt", default="Explain quantum entanglement in simple terms.", help="Prompt for inference")
    parser.add_argument("--threads", type=int, default=8, help="Number of local threads")
    
    args = parser.parse_args()
    
    rpc_endpoints = args.rpc
    if not rpc_endpoints:
        import socket
        print(f"Waiting for {args.num_devices} devices to send UDP discovery pings...")
        discovered_endpoints = set()
        
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.bind(("0.0.0.0", 50055))
        sock.settimeout(10.0)
        
        try:
            while len(discovered_endpoints) < args.num_devices:
                data, addr = sock.recvfrom(1024)
                msg = data.decode('utf-8')
                if msg.startswith("llama-rpc-ping:"):
                    port = msg.split(":")[1]
                    endpoint = f"{addr[0]}:{port}"
                    if endpoint not in discovered_endpoints:
                        print(f"Discovered device: {endpoint}")
                        discovered_endpoints.add(endpoint)
        except socket.timeout:
            print(f"Error: Timed out waiting for {args.num_devices} devices. Only found {len(discovered_endpoints)}.")
            sock.close()
            return
            
        sock.close()
        rpc_endpoints = ",".join(discovered_endpoints)
        print(f"All devices discovered: {rpc_endpoints}")

    llama_cli_path = "/home/angela/Code/ggml/llama_cpp/build/bin/llama-cli"
    if not os.path.exists(llama_cli_path):
        # Fallback to just llama-cli in PATH
        llama_cli_path = "llama-cli"

    cmd = [
        llama_cli_path,
        "-m", args.model,
        "-p", args.prompt,
        "-t", str(args.threads),
        "--rpc", rpc_endpoints
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
