-- ==============================================================================
-- CHATIVA - SKEMA DATABASE SUPABASE (IDEMPOTENT & AMAN DIJALANKAN BERULANG)
-- Jalankan skrip SQL ini di SQL Editor pada Supabase Dashboard Anda.
-- ==============================================================================

-- 1. AKTIFKAN EXTENSION (jika belum)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. TABEL USERS (Profil Pengguna)
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    email TEXT NOT NULL DEFAULT '',
    phone_number TEXT DEFAULT '',
    about TEXT DEFAULT 'Halo! Saya menggunakan CHATIVA.',
    profile_pic_url TEXT DEFAULT '',
    is_online BOOLEAN DEFAULT false,
    last_seen BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    is_typing_to TEXT,
    is_email_verified BOOLEAN DEFAULT false,
    account_status TEXT DEFAULT 'Aktif',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index untuk pencarian user
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON public.users(phone_number);

-- 3. TABEL CHATS (Percakapan)
CREATE TABLE IF NOT EXISTS public.chats (
    id TEXT PRIMARY KEY,
    participant_ids JSONB DEFAULT '[]'::JSONB,
    participant_names JSONB DEFAULT '{}'::JSONB,
    participant_avatars JSONB DEFAULT '{}'::JSONB,
    last_message TEXT DEFAULT '',
    last_message_time BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    unread_counts JSONB DEFAULT '{}'::JSONB,
    typing_users JSONB DEFAULT '[]'::JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index untuk filter chat
CREATE INDEX IF NOT EXISTS idx_chats_last_message_time ON public.chats(last_message_time DESC);

-- 4. TABEL MESSAGES (Pesan Chat)
CREATE TABLE IF NOT EXISTS public.messages (
    id TEXT PRIMARY KEY,
    chat_id TEXT NOT NULL REFERENCES public.chats(id) ON DELETE CASCADE,
    sender_id TEXT NOT NULL,
    receiver_id TEXT NOT NULL,
    sender_name TEXT DEFAULT '',
    text TEXT DEFAULT '',
    type TEXT DEFAULT 'TEXT', -- TEXT, IMAGE, VIDEO, DOCUMENT, VOICE, LOCATION
    media_url TEXT DEFAULT '',
    file_name TEXT DEFAULT '',
    file_size TEXT DEFAULT '',
    voice_duration_sec INTEGER DEFAULT 0,
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    status TEXT DEFAULT 'SENT', -- SENT, DELIVERED, READ
    reply_to_id TEXT,
    reply_to_text TEXT,
    reply_to_sender TEXT,
    is_edited BOOLEAN DEFAULT false,
    is_forwarded BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index untuk messages
CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON public.messages(chat_id);
CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON public.messages(timestamp ASC);

-- 5. AKTIFKAN SUPABASE REALTIME (Dicek agar tidak error jika sudah ada)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'chats'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.chats;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'messages'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.messages;
    END IF;
END $$;

-- 6. PENGATURAN ROW LEVEL SECURITY (RLS)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chats ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Drop policy lama jika sudah ada agar tidak duplikat
DROP POLICY IF EXISTS "Public read users" ON public.users;
DROP POLICY IF EXISTS "Users can insert own profile" ON public.users;
DROP POLICY IF EXISTS "Users can update own profile" ON public.users;

DROP POLICY IF EXISTS "Allow all actions on chats" ON public.chats;
DROP POLICY IF EXISTS "Allow all actions on messages" ON public.messages;

-- Policy untuk USERS
CREATE POLICY "Public read users" ON public.users FOR SELECT USING (true);
CREATE POLICY "Users can insert own profile" ON public.users FOR INSERT WITH CHECK (true);
CREATE POLICY "Users can update own profile" ON public.users FOR UPDATE USING (true);

-- Policy untuk CHATS
CREATE POLICY "Allow all actions on chats" ON public.chats FOR ALL USING (true);

-- Policy untuk MESSAGES
CREATE POLICY "Allow all actions on messages" ON public.messages FOR ALL USING (true);

-- 7. TRIGGER OTOMATIS: Salin Akun Baru dari auth.users ke public.users
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, name, email, phone_number, is_online, last_seen, is_email_verified)
    VALUES (
        NEW.id::TEXT,
        COALESCE(NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1)),
        COALESCE(NEW.email, ''),
        COALESCE(NEW.raw_user_meta_data->>'phone', ''),
        true,
        (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
        (NEW.email_confirmed_at IS NOT NULL)
    )
    ON CONFLICT (id) DO UPDATE SET
        name = COALESCE(EXCLUDED.name, public.users.name),
        email = COALESCE(EXCLUDED.email, public.users.email),
        updated_at = NOW();
    RETURN NEW;
EXCEPTION WHEN OTHERS THEN
    -- Jangan blokir proses auth meskipun insert profil gagal
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Pasang trigger di auth.users
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 8. STORAGE BUCKET: Untuk Media Chat & Avatar
INSERT INTO storage.buckets (id, name, public)
VALUES ('chativa-media', 'chativa-media', true)
ON CONFLICT (id) DO NOTHING;

-- Drop policy lama storage jika sudah ada
DROP POLICY IF EXISTS "Public Access Storage" ON storage.objects;
DROP POLICY IF EXISTS "Allow upload to chativa-media" ON storage.objects;
DROP POLICY IF EXISTS "Allow update/delete in chativa-media" ON storage.objects;

-- Policy Storage
CREATE POLICY "Public Access Storage" ON storage.objects
FOR SELECT USING (bucket_id = 'chativa-media');

CREATE POLICY "Allow upload to chativa-media" ON storage.objects
FOR INSERT WITH CHECK (bucket_id = 'chativa-media');

CREATE POLICY "Allow update/delete in chativa-media" ON storage.objects
FOR ALL USING (bucket_id = 'chativa-media');
