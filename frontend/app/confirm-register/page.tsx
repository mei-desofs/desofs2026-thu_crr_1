'use client';

import { useEffect, useState } from 'react';
import { apiPost } from '@/lib/api';
import { useToast } from '@/app/components/useToast';

interface ConfirmResponse {
  message: string;
}

export default function ConfirmPage() {
  const [title, setTitle] = useState('⏳ A verificar...');
  const [message, setMessage] = useState('Por favor aguarda.');
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const { success, error } = useToast();

  useEffect(() => {
    let mounted = true;

    const confirmEmail = async () => {
      if (!mounted) return;

      const hash = window.location.hash.substring(1);
      const params = new URLSearchParams(hash);
      const accessToken = params.get('access_token');
      const type = params.get('type');

      if (!accessToken || type !== 'signup') {
        if (!mounted) return;
        setTitle('❌ Link inválido');
        setMessage('Este link não é válido ou já foi usado.');
        setStatus('error');
        error('Link inválido');
        return;
      }

      try {
        await apiPost<ConfirmResponse>('/auth/confirm', { accessToken });
        
        if (!mounted) return;
        setTitle('✓ Email confirmado!');
        setMessage('A tua conta foi criada e verificada. Podes fechar esta página.');
        setStatus('success');
        success('Email confirmado com sucesso!');
      } catch (err: any) {
        if (!mounted) return;
        
        console.error('Confirm email failed:', err);
        const errorMsg = err?.response?.data?.error || 'Ocorreu um erro. Tenta novamente.';
        setTitle('❌ Erro na confirmação');
        setMessage(errorMsg);
        setStatus('error');
        error(errorMsg);
      }
    };

    confirmEmail();

    return () => {
      mounted = false;
    };
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