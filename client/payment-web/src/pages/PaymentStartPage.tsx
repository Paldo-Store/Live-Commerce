import { useEffect, useState } from "react";
import { createOrder, getBroadcastProducts, getProductDetail, readyPayment, searchBroadcasts, signIn } from "../lib/paymentApi";
import { readPaymentHeaders, savePaymentHeaders, savePaymentSession } from "../lib/paymentSession";
import type { BroadcastProductSummary, BroadcastSummary, PaymentHeaders, PaymentMethod, ProductDetail } from "../types/payment";

declare const TossPayments: (clientKey: string) => {
  requestPayment: (method: string, params: Record<string, unknown>) => Promise<void>;
};
const TOSS_CLIENT_KEY = "test_ck_GjLJoQ1aVZJGJxea9RQQVw6KYe2R";

function emptyLog() {
  return "{\n  \"status\": \"idle\"\n}";
}

function formatPrice(value: number) {
  return `${value.toLocaleString()}원`;
}

function summarizeToken(token: string) {
  if (!token.trim()) {
    return "없음";
  }

  if (token.length <= 24) {
    return token;
  }

  return `${token.slice(0, 18)}...${token.slice(-8)}`;
}

function parseJwtPayload(token: string) {
  if (!token.trim()) {
    return null;
  }

  const normalized = token.startsWith("Bearer ") ? token.slice(7) : token;
  const parts = normalized.split(".");
  if (parts.length < 2) {
    return null;
  }

  try {
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
    const decoded = atob(padded);
    return JSON.parse(decoded) as { userId?: string; username?: string; role?: string };
  } catch {
    return null;
  }
}

const DEFAULT_BROADCAST_ID = "10000000-0000-0000-0000-0000000000a1";
const DEFAULT_PRODUCT_ID = "30000000-0000-0000-0000-000000000004";
const DEFAULT_ORDER_QUANTITY = 2;
const DEFAULT_REQUIREMENT = "방송 테스트 주문입니다";

function resolveRouteParams() {
  const pathMatch = window.location.pathname.match(/^\/products\/([^/]+)$/);
  const query = new URLSearchParams(window.location.search);

  return {
    productId: pathMatch?.[1] ?? query.get("productId") ?? DEFAULT_PRODUCT_ID,
    broadcastId: query.get("broadcastId") ?? DEFAULT_BROADCAST_ID,
  };
}

export function PaymentStartPage() {
  const routeParams = resolveRouteParams();
  const [headers, setHeaders] = useState<PaymentHeaders>(() => readPaymentHeaders());
  const [broadcastId, setBroadcastId] = useState(routeParams.broadcastId);
  const [productId, setProductId] = useState(routeParams.productId);
  const [orderQuantity, setOrderQuantity] = useState(DEFAULT_ORDER_QUANTITY);
  const [requirement, setRequirement] = useState(DEFAULT_REQUIREMENT);
  const [couponId, setCouponId] = useState("");
  const [orderId, setOrderId] = useState("");
  const [amount, setAmount] = useState(1000);
  const [itemName, setItemName] = useState("테스트 상품");
  const [message, setMessage] = useState("로그인 후 상품을 선택해 주문할 수 있습니다.");
  const [log, setLog] = useState(emptyLog());
  const [isLoading, setIsLoading] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("TOSS");
  const [broadcasts, setBroadcasts] = useState<BroadcastSummary[]>([]);
  const [broadcastProducts, setBroadcastProducts] = useState<BroadcastProductSummary[]>([]);
  const [activeBroadcast, setActiveBroadcast] = useState<BroadcastSummary | null>(null);
  const [activeProduct, setActiveProduct] = useState<BroadcastProductSummary | null>(null);
  const [productDetail, setProductDetail] = useState<ProductDetail | null>(null);
  const [showDevPanel, setShowDevPanel] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const persistHeaders = () => {
    savePaymentHeaders(headers);
    setMessage("토큰을 저장했습니다.");
  };

  const logout = () => {
    setHeaders({ authorization: "" });
    savePaymentHeaders({ authorization: "" });
    setUsername("");
    setPassword("");
    setMessage("로그아웃되었습니다.");
  };

  const login = async () => {
    if (!username.trim() || !password.trim()) {
      setMessage("아이디와 비밀번호를 입력하세요.");
      setLog(JSON.stringify({ error: "missing credentials" }, null, 2));
      return;
    }

    setIsLoading(true);
    setMessage("로그인 중입니다.");

    try {
      const response = await signIn({ username, password });
      const nextHeaders: PaymentHeaders = {
        authorization: response.data.accessToken,
        refreshToken: response.data.refreshToken,
      };

      setHeaders(nextHeaders);
      savePaymentHeaders(nextHeaders);
      setLog(JSON.stringify(response, null, 2));
      setMessage("로그인 성공. 바로 구매할 수 있습니다.");
    } catch (error) {
      const nextMessage = error instanceof Error ? error.message : "로그인 실패";
      setMessage(nextMessage);
      setLog(JSON.stringify({ error: nextMessage }, null, 2));
    } finally {
      setIsLoading(false);
    }
  };

  const loadProductDetail = async (nextProductId: string) => {
    if (!nextProductId.trim()) {
      return;
    }

    if (!headers.authorization.trim()) {
      return;
    }

    try {
      const response = await getProductDetail(nextProductId, headers);
      setProductDetail(response.data);
      setItemName(response.data.name);
      setAmount(response.data.price);
      setActiveProduct((current) =>
        current && current.productId === response.data.productId
          ? current
          : {
              productId: response.data.productId,
              name: response.data.name,
              price: response.data.price,
            },
      );
    } catch (error) {
      const nextMessage = error instanceof Error ? error.message : "상품 상세 조회 실패";
      setMessage(nextMessage);
      setLog(JSON.stringify({ error: nextMessage }, null, 2));
    }
  };

  const loadBroadcastProducts = async (nextBroadcastId?: string) => {
    const targetBroadcastId = nextBroadcastId ?? broadcastId;
    if (!targetBroadcastId.trim()) {
      return;
    }

    if (!headers.authorization.trim()) {
      return;
    }

    try {
      const response = await getBroadcastProducts(targetBroadcastId, headers);
      const products = response.data.products;
      setBroadcastProducts(products);
      setLog(JSON.stringify(response, null, 2));

      if (!activeBroadcast || activeBroadcast.liveBroadcastId !== targetBroadcastId) {
        setActiveBroadcast({
          liveBroadcastId: targetBroadcastId,
          broadcastName: "선택한 방송",
          broadcastStatus: "UNKNOWN",
          companyId: "",
        });
      }

      const firstProduct = products[0] ?? null;
      const preferredProduct =
        products.find((product) => product.productId === routeParams.productId)
        ?? products.find((product) => product.productId === DEFAULT_PRODUCT_ID)
        ?? firstProduct;
      setActiveProduct(preferredProduct);
      if (preferredProduct) {
        setProductId(preferredProduct.productId);
        setItemName(preferredProduct.name);
        setAmount(preferredProduct.price);
        await loadProductDetail(preferredProduct.productId);
      }
    } catch (error) {
      const nextMessage = error instanceof Error ? error.message : "방송 상품 조회 실패";
      setMessage(nextMessage);
      setLog(JSON.stringify({ error: nextMessage }, null, 2));
    }
  };

  const loadBroadcasts = async () => {
    if (!headers.authorization.trim()) {
      return;
    }

    try {
      const response = await searchBroadcasts(headers);
      const items = response.data.liveBroadcasts;
      setBroadcasts(items);
      setLog(JSON.stringify(response, null, 2));

      const liveBroadcast = items.find((broadcast) => broadcast.broadcastStatus === "LIVE") ?? null;
      const linkedBroadcast = broadcastId
        ? items.find((broadcast) => broadcast.liveBroadcastId === broadcastId) ?? null
        : null;
      const chosenBroadcast = linkedBroadcast ?? liveBroadcast;

      setActiveBroadcast(chosenBroadcast);

      if (broadcastId) {
        await loadBroadcastProducts(broadcastId);
        setMessage("상품 정보를 불러왔습니다.");
      } else if (chosenBroadcast) {
        setBroadcastId(chosenBroadcast.liveBroadcastId);
        await loadBroadcastProducts(chosenBroadcast.liveBroadcastId);
        setMessage("라이브 상품을 불러왔습니다.");
      } else {
        setMessage("현재 LIVE 방송이 없습니다. 개발 패널에서 수동 확인 가능합니다.");
      }
    } catch (error) {
      const nextMessage = error instanceof Error ? error.message : "방송 조회 실패";
      setMessage(nextMessage);
      setLog(JSON.stringify({ error: nextMessage }, null, 2));

      if (broadcastId) {
        await loadBroadcastProducts(broadcastId);
      }
    }
  };

  useEffect(() => {
    void loadProductDetail(routeParams.productId);
  }, []);

  const submitCreateOrder = async () => {
    if (!headers.authorization.trim()) {
      setMessage("먼저 로그인하세요.");
      return;
    }

    if (!broadcastId.trim() || !productId.trim()) {
      setMessage("주문 가능한 방송/상품이 없습니다.");
      return;
    }

    setIsLoading(true);
    setMessage("주문 생성 중입니다.");

    try {
      savePaymentHeaders(headers);
      const response = await createOrder(
        {
          productId,
          orderQuantity,
          requirement,
          broadcastId,
          couponId: couponId.trim() ? couponId.trim() : null,
        },
        headers,
      );

      const chosenProduct = broadcastProducts.find((product) => product.productId === productId);
      setOrderId(response.data.orderId);
      setAmount(response.data.finalPaidPrice || response.data.productTotalPrice);
      setItemName(chosenProduct?.name ?? itemName);
      setLog(JSON.stringify(response, null, 2));
      setMessage("주문 생성 완료. 결제창으로 이동합니다.");
    } catch (error) {
      const nextMessage = error instanceof Error ? error.message : "주문 생성 실패";
      setMessage(nextMessage);
      setLog(JSON.stringify({ error: nextMessage }, null, 2));
    } finally {
      setIsLoading(false);
    }
  };

  const submitReady = async () => {
    if (!headers.authorization.trim()) {
      setMessage("먼저 로그인하세요.");
      return;
    }

    setIsLoading(true);

    try {
      savePaymentHeaders(headers);
      const response = await readyPayment({ orderId, amount, itemName, paymentMethod }, headers);
      const redirectUrl = response.data.next_redirect_pc_url ?? response.data.nextRedirectUrl ?? "";

      savePaymentSession({
        orderId,
        tid: response.data.tid,
        amount,
        itemName,
        redirectUrl,
        paymentMethod,
      });

      setLog(JSON.stringify(response, null, 2));

      if (paymentMethod === "TOSS") {
        const toss = TossPayments(TOSS_CLIENT_KEY);
        await toss.requestPayment("카드", {
          amount,
          orderId,
          orderName: itemName,
          successUrl: `${window.location.origin}/payments/approve`,
          failUrl: `${window.location.origin}/payments/fail`,
        });
        return;
      }

      if (!redirectUrl) {
        throw new Error("redirect url이 없습니다.");
      }

      window.location.href = redirectUrl;
    } catch (error) {
      const nextMessage = error instanceof Error ? error.message : "결제 준비 실패";
      setMessage(nextMessage);
      setLog(JSON.stringify({ error: nextMessage }, null, 2));
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (!headers.authorization.trim()) {
      return;
    }

    void loadProductDetail(productId);
    void loadBroadcasts();
  }, [headers.authorization, productId]);

  useEffect(() => {
    if (!orderId) {
      return;
    }

    void submitReady();
  }, [orderId]);

  const displayProduct = productDetail
    ? {
        productId: productDetail.productId,
        name: productDetail.name,
        price: productDetail.price,
      }
    : activeProduct;
  const currentTotal = displayProduct ? displayProduct.price * orderQuantity : 0;
  const rocketArrival = "내일(수) 7/3 도착 보장";
  const isLoggedIn = headers.authorization.trim().length > 0;
  const authUser = parseJwtPayload(headers.authorization);
  const displayUsername = authUser?.username ?? "";
  const displayRole = authUser?.role ?? "";

  return (
    <main className="shell">
      <header className="topbar">
        <div className="brand">LIVE COMMERCE</div>
        <div className="topbar-actions">
          <button className="ghost" type="button" onClick={() => void loadBroadcasts()}>
            상품 새로고침
          </button>
          <button className="ghost" type="button" onClick={() => setShowDevPanel((prev) => !prev)}>
            {showDevPanel ? "개발 패널 숨기기" : "개발 패널"}
          </button>
        </div>
      </header>

      <section className="login-box">
        <div className="login-copy">
          <strong>{isLoggedIn && displayUsername ? `${displayUsername}님, 안녕하세요` : "간단 로그인"}</strong>
          <span>
            {isLoggedIn
              ? "지금 로그인된 계정으로 바로 주문/결제를 진행할 수 있습니다."
              : "로그인 후 바로 주문/결제를 테스트할 수 있습니다."}
          </span>
        </div>
        {isLoggedIn ? (
          <div className="login-actions">
            <button className="ghost" type="button" onClick={logout}>
              로그아웃
            </button>
          </div>
        ) : (
          <div className="login-form">
            <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="아이디" />
            <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" placeholder="비밀번호" />
            <button type="button" onClick={() => void login()} disabled={isLoading}>
              {isLoading ? "처리 중..." : "로그인"}
            </button>
          </div>
        )}
      </section>

      <section className="page">
        <div className="product-area">
          <article className="gallery-panel">
            <div className="main-image">
              <span>{displayProduct?.name.slice(0, 1) ?? "?"}</span>
            </div>
            <div className="thumb-list">
              <div className="thumb-item active" />
              <div className="thumb-item" />
              <div className="thumb-item" />
            </div>
          </article>

          <article className="info-panel">
            <div className="info-head">
              <span className="live-tag">LIVE</span>
              <span className="live-name">{activeBroadcast?.broadcastName ?? "진행 중인 라이브 없음"}</span>
            </div>

            <h1 className="product-name">{displayProduct?.name ?? "상품이 없습니다"}</h1>
            <div className="rating-row">
              <span className="rating-score">4.8</span>
              <span className="rating-stars">★★★★★</span>
              <span className="review-count">리뷰 12,345</span>
            </div>

            <div className="price-box">
              <div className="discount-line">
                <span className="discount-rate">18%</span>
                <strong className="sale-price">{activeProduct ? formatPrice(activeProduct.price) : "-"}</strong>
              </div>
              <div className="sub-price">정가 39,900원 · 라이브 특가 적용</div>
            </div>

            <div className="benefit-box">
              <div>
                <span>배송</span>
                <strong>{rocketArrival}</strong>
              </div>
              <div>
                <span>혜택</span>
                <strong>라이브 전용 즉시할인 적용 가능</strong>
              </div>
              <div>
                <span>카테고리</span>
                <strong>{productDetail?.category ?? "-"}</strong>
              </div>
              <div>
                <span>판매상태</span>
                <strong>{productDetail?.status ?? "-"}</strong>
              </div>
              <div>
                <span>품절 여부</span>
                <strong>{productDetail ? (productDetail.soldOut ? "품절" : "판매 가능") : "-"}</strong>
              </div>
              <div>
                <span>상품번호</span>
                <strong>{productDetail?.productId ?? productId}</strong>
              </div>
            </div>

            <div className="description-box">
              {productDetail?.description ?? "상품 설명이 아직 없습니다."}
            </div>
          </article>
        </div>

        <aside className="buy-panel">
          <div className="buy-card">
            <div className="buy-title-row">
              <strong>{displayProduct?.name ?? "상품 준비 중"}</strong>
              <span>{displayProduct ? formatPrice(displayProduct.price) : "-"}</span>
            </div>

            <label className="field">
              <span>수량</span>
              <input value={orderQuantity} onChange={(e) => setOrderQuantity(Number(e.target.value))} type="number" min="1" />
            </label>

            <label className="field">
              <span>쿠폰 ID</span>
              <input value={couponId} onChange={(e) => setCouponId(e.target.value)} placeholder="없으면 비워두기" />
            </label>

            <label className="field">
              <span>배송 요청사항</span>
              <input value={requirement} onChange={(e) => setRequirement(e.target.value)} placeholder="문 앞에 놓아주세요" />
            </label>

            <div className="total-summary">
              <span>총 상품금액</span>
              <strong>{formatPrice(currentTotal)}</strong>
            </div>

            <div className="field" style={{ display: "flex", gap: "8px" }}>
              <button
                type="button"
                className={paymentMethod === "TOSS" ? "primary-buy" : "secondary-buy"}
                style={{ flex: 1, padding: "10px 0" }}
                onClick={() => setPaymentMethod("TOSS")}
              >
                토스페이먼츠
              </button>
              <button
                type="button"
                className={paymentMethod === "KAKAO" ? "primary-buy" : "secondary-buy"}
                style={{ flex: 1, padding: "10px 0" }}
                onClick={() => setPaymentMethod("KAKAO")}
              >
                카카오페이
              </button>
            </div>

            <button className="primary-buy" type="button" onClick={() => void submitCreateOrder()} disabled={isLoading || !displayProduct}>
              {isLoading ? "처리 중..." : "주문하기"}
            </button>

            <button className="secondary-buy" type="button">
              장바구니
            </button>

            <p className="helper-text">{message}</p>
          </div>
        </aside>
      </section>

      <section className="related-section">
        <div className="section-title-row">
          <h2>함께 보고 있는 상품</h2>
        </div>
        {broadcastProducts.length > 0 ? (
          <div className="related-grid">
            {broadcastProducts.map((product) => {
              const selected = (productDetail?.productId ?? activeProduct?.productId) === product.productId;
              return (
                <button
                  key={product.productId}
                  type="button"
                  className={`related-card ${selected ? "selected" : ""}`}
                  onClick={() => {
                    const nextUrl = `/products/${product.productId}?broadcastId=${broadcastId || DEFAULT_BROADCAST_ID}`;
                    window.location.href = nextUrl;
                  }}
                >
                  <div className="related-image">
                    <span>{product.name.slice(0, 1)}</span>
                  </div>
                  <strong>{product.name}</strong>
                  <span>{formatPrice(product.price)}</span>
                </button>
              );
            })}
          </div>
        ) : (
          <div className="empty-box">현재 연결된 상품이 없습니다.</div>
        )}
      </section>

      {showDevPanel ? (
        <section className="dev-section">
          <article className="panel dev-panel">
            <h2>개발 패널</h2>
            <div className="inline-actions">
              <button className="secondary" type="button" onClick={() => void loadBroadcasts()}>
                방송 새로고침
              </button>
              <button className="secondary" type="button" onClick={() => void loadBroadcastProducts()}>
                상품 새로고침
              </button>
              <button className="secondary" type="button" onClick={persistHeaders}>
                토큰 저장
              </button>
            </div>
            <label className="field">
              <span>Access Token</span>
              <input value={headers.authorization} onChange={(e) => setHeaders({ authorization: e.target.value })} placeholder="Bearer eyJ..." />
            </label>
            <label className="field">
              <span>broadcastId</span>
              <input value={broadcastId} onChange={(e) => setBroadcastId(e.target.value)} placeholder="라이브 방송 UUID" />
            </label>
              <label className="field">
                <span>productId</span>
                <input value={productId} onChange={(e) => setProductId(e.target.value)} placeholder="상품 UUID" />
              </label>
              <div className="inline-actions">
                <button
                  className="secondary"
                  type="button"
                  onClick={() => {
                    const nextUrl = `/products/${productId}?broadcastId=${broadcastId || DEFAULT_BROADCAST_ID}`;
                    window.location.href = nextUrl;
                  }}
                >
                  해당 상품 URL로 이동
                </button>
              </div>
              <label className="field">
                <span>orderId</span>
                <input value={orderId} onChange={(e) => setOrderId(e.target.value)} placeholder="자동 생성되거나 직접 입력" />
            </label>
            <div className="chip-list">
              {broadcasts.map((broadcast) => (
                <button
                  key={broadcast.liveBroadcastId}
                  className={`chip ${broadcast.broadcastStatus === "LIVE" ? "chip-live" : ""}`}
                  type="button"
                  onClick={() => {
                    setBroadcastId(broadcast.liveBroadcastId);
                    setActiveBroadcast(broadcast);
                    void loadBroadcastProducts(broadcast.liveBroadcastId);
                  }}
                >
                  {broadcast.broadcastName} · {broadcast.broadcastStatus}
                </button>
              ))}
            </div>
          </article>

          <article className="panel log-panel">
            <h2>응답 로그</h2>
            <pre>{log}</pre>
          </article>
        </section>
      ) : null}
    </main>
  );
}
