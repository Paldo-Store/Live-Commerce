export function PaymentFailPage() {
  return (
    <main className="shell shell-narrow">
      <section className="panel center-panel">
        <p className="eyebrow">Fail Callback</p>
        <h1>결제 승인에 실패했습니다.</h1>
        <p className="hero-copy">카카오가 실패 콜백을 보냈거나 승인 단계에서 값이 맞지 않을 때 확인하는 화면입니다.</p>
        <a className="button-link" href="/">
          다시 시도
        </a>
      </section>
    </main>
  );
}
