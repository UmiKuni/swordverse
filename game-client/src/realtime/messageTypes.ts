/** Defines typed STOMP command and event envelopes from the API/WebSocket contract. */
export type SystemStatusEvent = {
  application: string;
  status: string;
  serverTime: string;
};
