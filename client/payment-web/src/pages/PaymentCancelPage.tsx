export function PaymentCancelPage() {
  return (
    <main className="shell shell-narrow">
      <section className="panel center-panel">
        <p className="eyebrow">Cancel Callback</p>
        <h1>사용자가 결제를 취소했습니다.</h1>
        <p className="hero-copy">카카오 결제창에서 취소된 경우 이 페이지로 돌아옵니다.</p>
        <a className="button-link" href="/">
          결제 화면으로 돌아가기
        </a>
      </section>
    </main>
  );
}
