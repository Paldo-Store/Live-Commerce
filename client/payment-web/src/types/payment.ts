export type PaymentHeaders = {
  authorization: string;
  refreshToken?: string;
};

export type SignInRequest = {
  username: string;
  password: string;
};

export type SignInPayload = {
  accessToken: string;
  refreshToken: string;
};

export type PaymentMethod = "KAKAO" | "TOSS";

export type PaymentReadyRequest = {
  orderId: string;
  amount: number;
  itemName: string;
  paymentMethod: PaymentMethod;
};

export type PaymentReadyPayload = {
  tid: string;
  next_redirect_pc_url?: string;
  nextRedirectUrl?: string;
};

export type ApiResponse<T> = {
  status: string;
  data: T;
};

export type PaymentApproveRequest = {
  tid: string;
  pgToken: string;
  orderId: string;
  amount: number;
};

export type PaymentSession = {
  orderId: string;
  tid: string;
  amount: number;
  itemName: string;
  redirectUrl: string;
  paymentMethod: PaymentMethod;
};

export type BroadcastSummary = {
  liveBroadcastId: string;
  broadcastName: string;
  broadcastStatus: string;
  companyId: string;
};

export type BroadcastSearchResponse = {
  liveBroadcasts: BroadcastSummary[];
  pagination: {
    currentPage: number;
    pageSize: number;
    totalPages: number;
    totalElements: number;
    isLast: boolean;
  };
};

export type BroadcastProductSummary = {
  productId: string;
  name: string;
  price: number;
};

export type ProductDetail = {
  productId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  status: string;
  companyId: string;
  soldOut: boolean;
};

export type BroadcastProductResponse = {
  products: BroadcastProductSummary[];
  pagination: {
    currentPage: number;
    pageSize: number;
    totalPages: number;
    totalElements: number;
    isLast: boolean;
  };
};

export type OrderCreateRequest = {
  productId: string;
  orderQuantity: number;
  requirement: string;
  broadcastId: string;
  couponId: string | null;
};

export type OrderCreatePayload = {
  orderId: string;
  productId: string;
  productQuantity: number;
  productTotalPrice: number;
  requirement: string;
  status: string;
  broadcastId: string;
  message: string;
  couponId: string | null;
  finalPaidPrice: number;
};
