'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';

export default function ConfirmPage() {
  const searchParams = useSearchParams();
  const [title, setTitle] = useState('⏳ A verificar...');
  const [message, setMessage] = useState('Por favor aguarda.');
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');

  useEffect(() => {
    const confirmEmail = async () => {
      const hash = window.location.hash.substring(1);
      const params = new URLSearchParams(hash);
      const accessToken = params.get('access_token');
      const type = params.get('type');

      if (!accessToken || type !== 'signup') {
        setTitle('❌ Link inválido');
        setMessage('Este link não é válido ou já foi usado.');
        setStatus('error');
        return;
      }

      try {
        const res = await fetch('/api/auth/confirm', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ accessToken })
        });

        if (res.ok) {
          setTitle('✓ Email confirmado!');
          setMessage('A tua conta foi criada e verificada. Podes fechar esta página.');
          setStatus('success');
        } else {
          const err = await res.json().catch(() => ({}));
          setTitle('❌ Erro na confirmação');
          setMessage(err.error || 'Ocorreu um erro. Tenta novamente.');
          setStatus('error');
        }
      } catch (e) {
        setTitle('❌ Erro de ligação');
        setMessage('Não foi possível contactar o servidor.');
        setStatus('error');
      }
    };

    confirmEmail();
  }, []);

  const statusColors = {
    success: 'text-green-500',
    error: 'text-red-500',
    loading: 'text-slate-400'
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-5">
      <div className="w-full max-w-md text-center p-10 bg-slate-800/60 rounded-2xl border border-slate-700/20">
        <h1 className={`text-4xl font-bold mb-3 ${statusColors[status]}`}>
          {title}
        </h1>
        <p className="text-lg text-slate-300 leading-relaxed mt-3">
          {message}
        </p>
      </div>
    </div>
  );
}