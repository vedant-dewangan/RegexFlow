import { useState } from 'react';
import './FAQ.css';

function FAQ() {
  const [openIndex, setOpenIndex] = useState(null);

  const faqs = [
    {
      question: "What is RegexFlow?",
      answer: "RegexFlow is MoneyView's intelligent SMS-to-Ledger engine that automatically converts unstructured bank transaction SMS alerts into structured, actionable financial data. It uses advanced regex pattern matching to extract transaction details with high accuracy."
    },
    {
      question: "How does RegexFlow process SMS alerts?",
      answer: "RegexFlow receives SMS alerts from banks, matches them against configured regex patterns specific to each bank, extracts transaction details (amount, date, type, merchant, balance), and creates structured ledger entries that are verified through a Maker-Checker workflow."
    },
    {
      question: "Which banks are supported?",
      answer: "RegexFlow supports multiple banks with customizable regex templates for each financial institution's unique SMS format. Administrators can add and manage banks through the Bank Management interface."
    },
    {
      question: "How do I create a regex template?",
      answer: "If you have Maker role access, you can create regex templates through the Maker Dashboard. The template builder allows you to create and test regex patterns for SMS parsing before submission for approval."
    },
    {
      question: "Is RegexFlow secure?",
      answer: "Yes, RegexFlow implements role-based access control to ensure secure access. All templates go through verification processes, and comprehensive audit trails track all changes and approvals for accountability."
    }
  ];

  const toggleFAQ = (index) => {
    setOpenIndex(openIndex === index ? null : index);
  };

  return (
    <section className="faq-section">
      <div className="container">
        <h2 className="faq-title">FAQs</h2>
        <p className="faq-subtitle">
          Find answers to common questions about RegexFlow
        </p>
        <div className="faq-list">
          {faqs.map((faq, index) => (
            <div key={index} className={`faq-item ${openIndex === index ? 'active' : ''}`}>
              <button 
                className="faq-question" 
                onClick={() => toggleFAQ(index)}
                aria-expanded={openIndex === index}
              >
                <span className="faq-question-text">{faq.question}</span>
                <span className="faq-icon">{openIndex === index ? '−' : '+'}</span>
              </button>
              <div className="faq-answer-wrapper">
                <div className="faq-answer">
                  {faq.answer}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default FAQ;
