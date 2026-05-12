import type {
  ApiResponse,
  BroadcastProductResponse,
  BroadcastSearchResponse,
  OrderCreatePayload,
  OrderCreateRequest,
  PaymentApproveRequest,
  PaymentHeaders,
  PaymentReadyPayload,
  PaymentReadyRequest,
  ProductDetail,
  SignInPayload,
  SignInRequest,
} from "../types/payment";

function buildHeaders(headers: PaymentHeaders): HeadersInit {
  const requestHeaders: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (headers.authorization) {
    requestHeaders.Authorization = headers.authorization;
  }

  return requestHeaders;
}

async function requestJson<T>(url: string, init: RequestInit): Promise<ApiResponse<T>> {
  const response = await fetch(url, init);
  const body = (await response.json()) as ApiResponse<T>;

  if (!response.ok) {
    const message = typeof body.data === "string" ? body.data : "request failed";
    throw new Error(message);
  }

  return body;
}

export function readyPayment(request: PaymentReadyRequest, headers: PaymentHeaders) {
  return requestJson<PaymentReadyPayload>("/api/v2/payments/ready", {
    method: "POST",
    headers: buildHeaders(headers),
    body: JSON.stringify(request),
  });
}

export function approvePayment(request: PaymentApproveRequest, headers: PaymentHeaders) {
  return requestJson("/api/v2/payments/approve", {
    method: "POST",
    headers: buildHeaders(headers),
    body: JSON.stringify(request),
  });
}

export function searchBroadcasts(headers: PaymentHeaders) {
  return requestJson<BroadcastSearchResponse>("/api/v1/livebroadcasts/search?page=0&size=10", {
    method: "GET",
    headers: buildHeaders(headers),
  });
}

export function getBroadcastProducts(broadcastId: string, headers: PaymentHeaders) {
  return requestJson<BroadcastProductResponse>(
    `/api/v1/livebroadcasts/${broadcastId}/products?page=0&size=10`,
    {
      method: "GET",
      headers: buildHeaders(headers),
    },
  );
}

export function createOrder(request: OrderCreateRequest, headers: PaymentHeaders) {
  return requestJson<OrderCreatePayload>("/api/v1/orders", {
    method: "POST",
    headers: buildHeaders(headers),
    body: JSON.stringify(request),
  });
}

export function getProductDetail(productId: string, headers: PaymentHeaders) {
  return requestJson<ProductDetail>(`/api/v1/products/${productId}`, {
    method: "GET",
    headers: buildHeaders(headers),
  });
}

export function signIn(request: SignInRequest) {
  return requestJson<SignInPayload>("/api/v1/auth/signin", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });
}
