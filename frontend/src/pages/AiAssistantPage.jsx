import React, { useState, useRef, useEffect } from 'react';
import { aiApi } from '../api';
import { useAuth } from '../AuthContext';
import { Link } from 'react-router-dom';
import { 
  Bot, 
  Send, 
  Sparkles, 
  User, 
  RefreshCw 
} from 'lucide-react';

let messageIdCounter = 100;
function createMessageId() {
  messageIdCounter += 1;
  return messageIdCounter;
}

export default function AiAssistantPage() {
  const { user } = useAuth();
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'bot',
      agentType: 'Scheme Discovery Agent',
      content: `Namaste${user?.fullName ? ' ' + user.fullName : ''}! I am your **SchemeSathi AI Sahayak** (योजना साथी).\n\nI can help you:\n* **Discover welfare schemes** suited to your family and profession\n* **Check exact eligibility criteria** and identify missing details\n* **Prepare required documents** without hassle\n* **Guide you through the application** on official government portals\n\nHow can I help you today? You can select a common query below or type your situation!`,
      suggestedActions: [
        { label: 'Check Farmer Schemes', prompt: 'I am a small farmer with 2 acres of land. What schemes am I eligible for?' },
        { label: 'College Scholarships', prompt: 'What scholarships are available for college students?' },
        { label: 'MUDRA Small Business Loan', prompt: 'How can I get a loan under Pradhan Mantri MUDRA Yojana?' },
        { label: 'Ayushman Bharat Healthcare', prompt: 'Who is eligible for Ayushman Bharat PM-JAY ₹5 lakh insurance?' }
      ]
    }
  ]);

  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [conversationId, setConversationId] = useState('conv_session_default');
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const handleSendMessage = async (textToSend) => {
    const text = textToSend || inputMessage;
    if (!text.trim() || loading) return;

    if (!user) {
      alert('Please log in or register to use the AI Sahayak assistant with your profile context. Or test our Demo Sandbox!');
      return;
    }

    const userMsg = {
      id: createMessageId(),
      sender: 'user',
      content: text
    };

    setMessages(prev => [...prev, userMsg]);
    setInputMessage('');
    setLoading(true);

    try {
      const res = await aiApi.chat(text, conversationId);
      if (res.data?.success) {
        const botData = res.data.data;
        const botMsg = {
          id: createMessageId(),
          sender: 'bot',
          agentType: botData.agentType || 'Policy Guidance Agent',
          content: botData.message,
          suggestedActions: botData.suggestedActions,
          sourceReferences: botData.sourceReferences
        };
        setMessages(prev => [...prev, botMsg]);
      }
    } catch {
      const errorMsg = {
        id: createMessageId(),
        sender: 'bot',
        agentType: 'System Support',
        content: 'I could not process that request right now. Please verify your connection or try again shortly.'
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container fade-in" style={{ padding: '30px 24px 60px', maxWidth: '1000px' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{
            width: '46px',
            height: '46px',
            borderRadius: '14px',
            background: 'linear-gradient(135deg, #f97316 0%, #8b5cf6 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 20px rgba(139, 92, 246, 0.3)'
          }}>
            <Bot size={28} color="#fff" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <h1 style={{ fontSize: '1.6rem', fontWeight: 800 }}>AI Sahayak</h1>
              <span className="badge badge-primary">Multi-Agent</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
              Deterministic Rule Enforcement + Multilingual Policy Explanations
            </p>
          </div>
        </div>

        <button
          onClick={() => {
            setConversationId('conv_' + createMessageId());
            setMessages([
              {
                id: 1,
                sender: 'bot',
                agentType: 'Scheme Discovery Agent',
                content: 'Conversation reset. How can I assist you with government schemes today?'
              }
            ]);
          }}
          className="btn btn-secondary btn-sm"
          style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
        >
          <RefreshCw size={14} /> New Chat
        </button>
      </div>

      {/* Chat Container */}
      <div className="glass-card" style={{
        display: 'flex',
        flexDirection: 'column',
        height: '650px',
        overflow: 'hidden'
      }}>
        {/* Messages Scroll Area */}
        <div style={{
          flex: 1,
          overflowY: 'auto',
          padding: '24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px'
        }}>
          {messages.map((msg) => (
            <div
              key={msg.id}
              style={{
                display: 'flex',
                flexDirection: msg.sender === 'user' ? 'row-reverse' : 'row',
                gap: '12px',
                alignItems: 'flex-start'
              }}
            >
              {/* Avatar */}
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: msg.sender === 'user' ? 'linear-gradient(135deg, #06b6d4, #3b82f6)' : 'linear-gradient(135deg, #f97316, #8b5cf6)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
                color: '#fff',
                fontSize: '0.85rem',
                fontWeight: 700
              }}>
                {msg.sender === 'user' ? <User size={18} /> : <Bot size={18} />}
              </div>

              {/* Message Bubble */}
              <div style={{
                maxWidth: '78%',
                background: msg.sender === 'user' ? 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)' : '#162035',
                border: msg.sender === 'user' ? 'none' : '1px solid rgba(255, 255, 255, 0.08)',
                color: '#fff',
                padding: '16px 20px',
                borderRadius: msg.sender === 'user' ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                fontSize: '0.92rem',
                lineHeight: 1.6,
                boxShadow: 'var(--shadow-sm)'
              }}>
                {/* Agent Tag for Bot */}
                {msg.sender === 'bot' && msg.agentType && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
                    <span style={{
                      fontSize: '0.68rem',
                      fontWeight: 700,
                      background: 'rgba(255, 255, 255, 0.08)',
                      color: '#38bdf8',
                      padding: '2px 8px',
                      borderRadius: '4px'
                    }}>
                      {msg.agentType}
                    </span>
                  </div>
                )}

                {/* Content */}
                <div style={{ whiteSpace: 'pre-line' }}>
                  {msg.content}
                </div>

                {/* Source References */}
                {msg.sourceReferences?.length > 0 && (
                  <div style={{ marginTop: '12px', paddingTop: '10px', borderTop: '1px solid rgba(255, 255, 255, 0.1)', fontSize: '0.78rem', color: 'var(--text-dim)' }}>
                    <strong>Sources:</strong> {msg.sourceReferences.join(', ')}
                  </div>
                )}

                {/* Suggested Prompt Chips */}
                {msg.suggestedActions?.length > 0 && (
                  <div style={{ marginTop: '14px', display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    {msg.suggestedActions.map((act, i) => (
                      <button
                        key={i}
                        onClick={() => handleSendMessage(act.prompt || act.label)}
                        style={{
                          background: 'rgba(255, 255, 255, 0.08)',
                          border: '1px solid rgba(255, 255, 255, 0.15)',
                          borderRadius: '8px',
                          padding: '6px 12px',
                          fontSize: '0.78rem',
                          color: '#fb923c',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '4px',
                          transition: 'all 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(249, 115, 22, 0.2)'}
                        onMouseLeave={(e) => e.currentTarget.style.background = 'rgba(255, 255, 255, 0.08)'}
                      >
                        <Sparkles size={13} /> {act.label}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}

          {loading && (
            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: '#162035',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <Bot size={18} color="var(--primary)" />
              </div>
              <div style={{
                background: '#162035',
                padding: '12px 18px',
                borderRadius: '16px',
                fontSize: '0.85rem',
                color: 'var(--text-muted)',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <RefreshCw size={14} className="spin" />
                AI Orchestrator is analyzing scheme policy and rules...
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Input Form */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSendMessage();
          }}
          style={{
            padding: '16px 20px',
            borderTop: '1px solid var(--border-subtle)',
            background: '#0d1322',
            display: 'flex',
            gap: '12px',
            alignItems: 'center'
          }}
        >
          <input
            type="text"
            placeholder={user ? "Ask anything about government schemes, eligibility, documents..." : "Please log in to chat with AI Sahayak..."}
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            disabled={!user || loading}
            style={{
              flex: 1,
              background: '#162035',
              padding: '12px 16px',
              fontSize: '0.95rem',
              borderRadius: '10px'
            }}
          />
          <button
            type="submit"
            disabled={!user || loading || !inputMessage.trim()}
            className="btn btn-primary"
            style={{ padding: '12px 20px', borderRadius: '10px' }}
          >
            <Send size={18} />
          </button>
        </form>
      </div>

      {!user && (
        <div style={{
          marginTop: '16px',
          textAlign: 'center',
          fontSize: '0.85rem',
          color: 'var(--text-muted)'
        }}>
          💡 Want to explore without signing up? Try the <Link to="/demo" style={{ color: 'var(--primary)', fontWeight: 700 }}>Sandbox Demo Personas</Link> first.
        </div>
      )}
    </div>
  );
}
