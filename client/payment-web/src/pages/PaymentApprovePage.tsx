import { useEffect, useState } from "react";
import { approvePayment } from "../lib/paymentApi";
import { clearPaymentSession, readPaymentHeaders, readPaymentSession } from "../lib/paymentSession";

export function PaymentApprovePage() {
  const [message, setMessage] = useState("승인 요청 준비 중입니다.");
  const [log, setLog] = useState("{\n  \"status\": \"pending\"\n}");

  useEffect(() => {
    const run = async () => {
      const params = new URLSearchParams(window.location.search);
      const paymentKey = params.get("paymentKey"); // Toss
      const pgToken = params.get("pg_token");       // KakaoPay
      const session = readPaymentSession();
      const headers = readPaymentHeaders();

      if (!session) {
        throw new Error("저장된 결제 세션이 없습니다.");
      }

      if (!paymentKey && !pgToken) {
        throw new Error("paymentKey 또는 pg_token이 없습니다.");
      }

      setMessage("approve 호출 중입니다.");

      const response = await approvePayment(
        {
          tid: session.tid,
          pgToken: paymentKey ?? pgToken!,
          orderId: params.get("orderId") ?? session.orderId,
          amount: Number(params.get("amount") ?? session.amount),
        },
        headers,
      );

      clearPaymentSession();
      setLog(JSON.stringify(response, null, 2));
      setMessage("결제가 완료되었습니다.");
    };

    run().catch((error: unknown) => {
      const nextMessage = error instanceof Error ? error.message : "approve 실패";
      setMessage(nextMessage);
      setLog(JSON.stringify({ error: nextMessage }, null, 2));
    });
  }, []);

  return (
    <main className="shell shell-narrow">
      <section className="panel center-panel">
        <p className="eyebrow">Approve Callback</p>
        <h1>{message}</h1>
        <pre>{log}</pre>
        <a className="button-link" href="/">
          다시 시작
        </a>
      </section>
    </main>
  );
}
