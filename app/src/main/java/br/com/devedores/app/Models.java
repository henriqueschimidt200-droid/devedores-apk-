package br.com.devedores.app;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Models {
    public static class Client {
        public String id = UUID.randomUUID().toString();
        public String name = "", cpf = "", rg = "", phone = "", email = "", address = "", notes = "", profileImagePath = "";
        public long createdAt = System.currentTimeMillis();
        public List<Document> documents = new ArrayList<>();
        public List<String> folders = new ArrayList<>();
        public List<Loan> loans = new ArrayList<>();

        public JSONObject toJson() {
            try {
                JSONObject o = new JSONObject();
                o.put("id", id); o.put("name", name); o.put("cpf", cpf); o.put("rg", rg);
                o.put("phone", phone); o.put("email", email); o.put("address", address); o.put("notes", notes);
                o.put("profileImagePath", profileImagePath); o.put("createdAt", createdAt);
                JSONArray docs = new JSONArray(); for (Document d : documents) docs.put(d.toJson()); o.put("documents", docs);
                JSONArray foldersJson = new JSONArray(); for (String f : folders) foldersJson.put(f); o.put("folders", foldersJson);
                JSONArray loansJson = new JSONArray(); for (Loan l : loans) loansJson.put(l.toJson()); o.put("loans", loansJson);
                return o;
            } catch (Exception e) { return new JSONObject(); }
        }

        public static Client fromJson(JSONObject o) {
            Client c = new Client();
            try {
                c.id = o.optString("id", c.id);
                c.name = o.optString("name", ""); c.cpf = o.optString("cpf", ""); c.rg = o.optString("rg", "");
                c.phone = o.optString("phone", ""); c.email = o.optString("email", ""); c.address = o.optString("address", "");
                c.notes = o.optString("notes", ""); c.profileImagePath = o.optString("profileImagePath", "");
                c.createdAt = o.optLong("createdAt", c.createdAt);
                JSONArray folderJson = o.optJSONArray("folders");
                if (folderJson != null) for (int i = 0; i < folderJson.length(); i++) c.folders.add(folderJson.optString(i));
                if (c.folders.isEmpty()) Collections.addAll(c.folders, "RG", "CPF", "Comprovante", "Contrato", "Fotos", "Outros");
                JSONArray docs = o.optJSONArray("documents");
                if (docs != null) for (int i = 0; i < docs.length(); i++) c.documents.add(Document.fromJson(docs.getJSONObject(i)));
                JSONArray oldDocs = o.optJSONArray("docs");
                if (oldDocs != null && c.documents.isEmpty()) {
                    for (int i = 0; i < oldDocs.length(); i++) {
                        Document d = new Document(); d.folder = "Outros"; d.name = "Documento"; d.uriOrPath = oldDocs.optString(i); c.documents.add(d);
                    }
                }
                JSONArray loans = o.optJSONArray("loans");
                if (loans != null) for (int i = 0; i < loans.length(); i++) c.loans.add(Loan.fromJson(loans.getJSONObject(i)));
            } catch (Exception ignored) {}
            return c;
        }
    }

    public static class Document {
        public String id = UUID.randomUUID().toString();
        public String folder = "Outros";
        public String name = "Documento";
        public String uriOrPath = "";
        public String mime = "application/octet-stream";
        public long addedAt = System.currentTimeMillis();
        public JSONObject toJson() {
            try { JSONObject o = new JSONObject(); o.put("id", id); o.put("folder", folder); o.put("name", name); o.put("uriOrPath", uriOrPath); o.put("mime", mime); o.put("addedAt", addedAt); return o; }
            catch (Exception e) { return new JSONObject(); }
        }
        public static Document fromJson(JSONObject o) { Document d = new Document(); d.id = o.optString("id", d.id); d.folder = o.optString("folder", "Outros"); d.name = o.optString("name", "Documento"); d.uriOrPath = o.optString("uriOrPath", ""); d.mime = o.optString("mime", "application/octet-stream"); d.addedAt = o.optLong("addedAt", d.addedAt); return d; }
    }

    public static class Loan {
        public String id = UUID.randomUUID().toString(), title = "Empréstimo", notes = "";
        public double principal, interestPercent, interestValue, total, installmentAmount;
        public String interestMode = "PERCENT", frequency = "Mensal";
        public int installments = 1;
        public long firstDue, createdAt = System.currentTimeMillis();
        public List<Payment> payments = new ArrayList<>();

        public double paid() { double s = 0; for (Payment p : payments) s += p.amount; return s; }
        public double balance() { return Math.max(0, total - paid()); }
        public long dueAt(int n) { java.util.Calendar c = java.util.Calendar.getInstance(); c.setTimeInMillis(firstDue); if ("Semanal".equals(frequency)) c.add(java.util.Calendar.DAY_OF_YEAR, 7 * n); else if ("Quinzenal".equals(frequency)) c.add(java.util.Calendar.DAY_OF_YEAR, 14 * n); else c.add(java.util.Calendar.MONTH, n); return c.getTimeInMillis(); }
        public double paidForInstallment(int n) { double before = 0, all = paid(); for (int i = 0; i < n; i++) before += installmentAmount; return Math.min(Math.max(0, all - before), installmentAmount); }
        public boolean installmentPaid(int n) { return paidForInstallment(n) + 0.005 >= installmentAmount; }

        public JSONObject toJson() {
            try {
                JSONObject o = new JSONObject();
                o.put("id", id); o.put("title", title); o.put("notes", notes); o.put("principal", principal);
                o.put("interestPercent", interestPercent); o.put("interestValue", interestValue); o.put("interestMode", interestMode);
                o.put("total", total); o.put("installmentAmount", installmentAmount); o.put("frequency", frequency);
                o.put("installments", installments); o.put("firstDue", firstDue); o.put("createdAt", createdAt);
                JSONArray p = new JSONArray(); for (Payment x : payments) p.put(x.toJson()); o.put("payments", p);
                return o;
            } catch (Exception e) { return new JSONObject(); }
        }

        public static Loan fromJson(JSONObject o) {
            Loan l = new Loan();
            try {
                l.id = o.optString("id", l.id); l.title = o.optString("title", "Empréstimo"); l.notes = o.optString("notes", "");
                l.principal = o.optDouble("principal"); l.interestPercent = o.optDouble("interestPercent"); l.interestValue = o.optDouble("interestValue");
                l.interestMode = o.optString("interestMode", "PERCENT"); l.total = o.optDouble("total"); l.installmentAmount = o.optDouble("installmentAmount");
                l.frequency = o.optString("frequency", "Mensal"); l.installments = o.optInt("installments", 1); l.firstDue = o.optLong("firstDue"); l.createdAt = o.optLong("createdAt", l.createdAt);
                JSONArray a = o.optJSONArray("payments"); if (a != null) for (int i = 0; i < a.length(); i++) l.payments.add(Payment.fromJson(a.getJSONObject(i)));
            } catch (Exception ignored) {}
            return l;
        }
    }

    public static class Payment {
        public String id = UUID.randomUUID().toString(), note = "";
        public double amount;
        public long date;
        JSONObject toJson() { try { JSONObject o = new JSONObject(); o.put("id", id); o.put("amount", amount); o.put("date", date); o.put("note", note); return o; } catch (Exception e) { return new JSONObject(); } }
        static Payment fromJson(JSONObject o) { Payment p = new Payment(); p.id = o.optString("id", p.id); p.amount = o.optDouble("amount"); p.date = o.optLong("date"); p.note = o.optString("note", ""); return p; }
    }

    public static class Reminder {
        public String id = UUID.randomUUID().toString(), title = "Lembrete", details = "";
        public long when;
        public boolean done;
        public JSONObject toJson() { try { JSONObject o = new JSONObject(); o.put("id", id); o.put("title", title); o.put("details", details); o.put("when", when); o.put("done", done); return o; } catch (Exception e) { return new JSONObject(); } }
        static Reminder fromJson(JSONObject o) { Reminder r = new Reminder(); r.id = o.optString("id", r.id); r.title = o.optString("title", "Lembrete"); r.details = o.optString("details", ""); r.when = o.optLong("when"); r.done = o.optBoolean("done", false); return r; }
    }
}
