using Microsoft.EntityFrameworkCore;
using SonzaiigiApi.Models;

namespace SonzaiigiApi.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<User> Users { get; set; }
    public DbSet<Post> Posts { get; set; }
    public DbSet<PostImage> PostImages { get; set; }
    public DbSet<Conversation> Conversations { get; set; }
    public DbSet<Message> Messages { get; set; }
    public DbSet<FavoriteGif> FavoriteGifs { get; set; }
    public DbSet<Reaction> Reactions { get; set; }
    public DbSet<DeletedMessage> DeletedMessages { get; set; }
    public DbSet<ConversationMember> ConversationMembers { get; set; }

    public DbSet<GroupRole> GroupRoles { get; set; }
    public DbSet<ConversationMemberRole> ConversationMemberRoles { get; set; }
    public DbSet<GroupBan> GroupBans { get; set; }
    public DbSet<GroupAuditLog> GroupAuditLogs { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        // НАЙДИ ЭТОТ БЛОК И ИЗМЕНИ ЕГО ВОТ ТАК:
        modelBuilder.Entity<ConversationMember>()
            .HasOne(cm => cm.User)
            .WithMany(u => u.ConversationMembers) // <-- Вот тут было пусто!
            .HasForeignKey(cm => cm.UserId);

        modelBuilder.Entity<ConversationMember>()
            .HasOne(cm => cm.Conversation)
            .WithMany(c => c.Members) // Указываем на List<ConversationMember> в Conversation
            .HasForeignKey(cm => cm.ConversationId);

        // Настройка уникальности: один юзер не может быть в одном чате дважды
        modelBuilder.Entity<ConversationMember>()
            .HasIndex(cm => new { cm.ConversationId, cm.UserId })
            .IsUnique();

        modelBuilder.Entity<Post>()
        .HasOne(p => p.User)
        .WithMany(u => u.Posts)
        .HasForeignKey(p => p.UserId)
        .OnDelete(DeleteBehavior.Cascade);

        // 2. Явно связываем Сообщения с Автором
        modelBuilder.Entity<Message>()
            .HasOne(m => m.User)
            .WithMany(u => u.Messages)
            .HasForeignKey(m => m.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        // 3. Явно связываем Изображения с Постом
        modelBuilder.Entity<PostImage>()
            .HasOne(pi => pi.Post)
            .WithMany(p => p.Images)
            .HasForeignKey(pi => pi.PostId)
            .OnDelete(DeleteBehavior.Cascade);

        // 1. Уникальные индексы пользователей
        modelBuilder.Entity<User>().HasIndex(u => u.Email).IsUnique();
        modelBuilder.Entity<User>().HasIndex(u => u.Username).IsUnique();
        modelBuilder.Entity<Conversation>().HasIndex(c => c.InviteToken).IsUnique();

        // 2. Лайки (post_user_likes)
        modelBuilder.Entity<User>()
            .HasMany(u => u.LikedPosts).WithMany(p => p.LikedByUsers)
            .UsingEntity(j => j.ToTable("PostUserLikes"));

  
        // 4. Подписки (Follows) с защитой от дубликатов
        modelBuilder.Entity<User>()
            .HasMany(u => u.Following).WithMany(u => u.Followers)
            .UsingEntity<Follow>(
                j => j.HasOne<User>().WithMany().HasForeignKey(f => f.FollowedId).OnDelete(DeleteBehavior.Restrict),
                j => j.HasOne<User>().WithMany().HasForeignKey(f => f.FollowerId).OnDelete(DeleteBehavior.Cascade),
                j => { j.HasKey(f => new { f.FollowerId, f.FollowedId }); j.ToTable("Follows"); }
            );

        // 5. Блокировки (Blocks)
        modelBuilder.Entity<User>()
            .HasMany(u => u.BlockedUsers).WithMany(u => u.BlockedBy)
            .UsingEntity<Block>(
                j => j.HasOne<User>().WithMany().HasForeignKey(b => b.BlockedId).OnDelete(DeleteBehavior.Restrict),
                j => j.HasOne<User>().WithMany().HasForeignKey(b => b.BlockerId).OnDelete(DeleteBehavior.Cascade),
                j => { j.HasKey(b => new { b.BlockerId, b.BlockedId }); j.ToTable("Blocks"); }
            );

        // 6. Муты (Mutes)
        modelBuilder.Entity<User>()
            .HasMany(u => u.MutedUsers).WithMany(u => u.MutedBy)
            .UsingEntity<Mute>(
                j => j.HasOne<User>().WithMany().HasForeignKey(m => m.MutedUserId).OnDelete(DeleteBehavior.Restrict),
                j => j.HasOne<User>().WithMany().HasForeignKey(m => m.UserId).OnDelete(DeleteBehavior.Cascade),
                j => { j.HasKey(m => new { m.UserId, m.MutedUserId }); j.ToTable("Mutes"); }
            );

        // 7. Ответы на сообщения (nullOnDelete как в Laravel)
        modelBuilder.Entity<Message>()
            .HasOne(m => m.Parent)
            .WithMany(m => m.Replies)
            .HasForeignKey(m => m.ParentId)
            .OnDelete(DeleteBehavior.SetNull);

        // 8. Уникальные реакции (один эмодзи на одно сообщение от юзера)
        modelBuilder.Entity<Reaction>()
            .HasIndex(r => new { r.MessageId, r.UserId, r.Emoji }).IsUnique();

        modelBuilder.Entity<ConversationMemberRole>()
        .HasKey(cmr => new { cmr.MemberId, cmr.RoleId });

        modelBuilder.Entity<ConversationMemberRole>()
            .HasOne(cmr => cmr.Member)
            .WithMany(m => m.MemberRoles)
            .HasForeignKey(cmr => cmr.MemberId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<ConversationMemberRole>()
            .HasOne(cmr => cmr.Role)
            .WithMany(r => r.MemberRoles)
            .HasForeignKey(cmr => cmr.RoleId)
            .OnDelete(DeleteBehavior.Cascade);

        // Удаление ролей, банов и логов при удалении группы
        modelBuilder.Entity<GroupRole>()
            .HasOne(r => r.Conversation)
            .WithMany(c => c.Roles)
            .HasForeignKey(r => r.ConversationId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<GroupBan>()
            .HasOne(b => b.Conversation)
            .WithMany(c => c.BannedUsers)
            .HasForeignKey(b => b.ConversationId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<GroupAuditLog>()
            .HasOne(a => a.Conversation)
            .WithMany(c => c.AuditLogs)
            .HasForeignKey(a => a.ConversationId)
            .OnDelete(DeleteBehavior.Cascade);
    }


}
