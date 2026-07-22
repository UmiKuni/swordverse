/** Diagnostic event published by SwordVerse on the system-status topic. */
export type SystemStatusEvent = {
  application: string;
  status: string;
  serverTime: string;
};
