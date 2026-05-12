import type { PaymentHeaders, PaymentSession } from "../types/payment";

const PAYMENT_SESSION_KEY = "payment-web-session";
const PAYMENT_HEADERS_KEY = "payment-web-headers";

export function readPaymentSession(): PaymentSession | null {
  const raw = localStorage.getItem(PAYMENT_SESSION_KEY);
  return raw ? (JSON.parse(raw) as PaymentSession) : null;
}

export function savePaymentSession(session: PaymentSession) {
  localStorage.setItem(PAYMENT_SESSION_KEY, JSON.stringify(session));
}

export function clearPaymentSession() {
  localStorage.removeItem(PAYMENT_SESSION_KEY);
}

export function readPaymentHeaders(): PaymentHeaders {
  const raw = localStorage.getItem(PAYMENT_HEADERS_KEY);
  if (!raw) {
    return {
      authorization: "",
    };
  }

  return JSON.parse(raw) as PaymentHeaders;
}

export function savePaymentHeaders(headers: PaymentHeaders) {
  localStorage.setItem(PAYMENT_HEADERS_KEY, JSON.stringify(headers));
}
