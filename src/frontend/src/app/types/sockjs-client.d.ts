declare module 'sockjs-client' {
  class SockJS {
    constructor(url: string, protocols?: string | string[], options?: any);
    onopen: ((event: any) => void) | null;
    onmessage: ((event: any) => void) | null;
    onclose: ((event: any) => void) | null;
    onerror: ((event: any) => void) | null;
    send(data: string): void;
    close(code?: number, reason?: string): void;
    readyState: number;
    protocol: string;
    url: string;
    addEventListener(type: string, listener: (event: any) => void): void;
    removeEventListener(type: string, listener: (event: any) => void): void;
  }
  export default SockJS;
}





