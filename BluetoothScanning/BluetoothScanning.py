from http.server import BaseHTTPRequestHandler, HTTPServer
import pexpect
import time
from urllib.parse import urlparse, parse_qs

PORT_NUMBER = 8080


class Handler(BaseHTTPRequestHandler):

    def do_GET(self):
        query_components = parse_qs(urlparse(self.path).query)

        if "task" in query_components:
            if query_components["task"][0] == "bluetooth":
                print("Scanning")
                child = pexpect.spawn("bluetoothctl")
                child.send("scan on\n")
                bdaddrs = []

                start_time = time.time()

                while True:
                    child.expect("Device (([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2}))")
                    bdaddr = child.match.group(1)

                    if bdaddr not in bdaddrs:
                        bdaddrs.append(bdaddr)

                    if time.time() - start_time >= 1000:
                        child.close()
                        break

                self.send_response(200)
                self.send_header('Content-type', 'text/html')
                self.end_headers()

                # Send message back to client
                message = str(len(bdaddrs))
                # Write content as utf-8 data
                self.wfile.write(bytes(message, "utf8"))
        else:
            self.send_response(404)

            self.send_header('Content-type', 'text/html')
            self.end_headers()

            # Send message back to client
            message = "No task found!"
            # Write content as utf-8 data
            self.wfile.write(bytes(message, "utf8"))

        return


def run():
    print('starting server...')

    # Server settings
    # Choose port 8080, for port 80, which is normally used for a http server, you need root access
    server_address = ('127.0.0.1', PORT_NUMBER)
    httpd = HTTPServer(server_address, Handler)
    print('running server...')
    httpd.serve_forever()


if __name__ == "__main__":
    run()
