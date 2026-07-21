package com.dolo.doctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dolo.doctor.data.model.UserRole
import com.dolo.doctor.hosted.*
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PageHeader
import com.dolo.doctor.ui.components.PrimaryAction
import com.dolo.doctor.ui.components.StatusPill
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable fun HostedStaffSyncScreen(localRole:UserRole,onBack:()->Unit,viewModel:HostedStaffViewModel){
 val state=viewModel.uiState;val profileState=viewModel.profileUiState;val scheduleState=viewModel.scheduleUiState;val visibleSnapshot=state.snapshot?.takeIf{HostedRoleBoundary.allows(localRole,it.role)};var pin by remember{mutableStateOf("")};var selectedSessionId by remember{mutableStateOf<String?>(null)}
 LaunchedEffect(localRole){viewModel.bindLocalRole(localRole)}
 LaunchedEffect(visibleSnapshot?.sessions){if(selectedSessionId==null)selectedSessionId=visibleSnapshot?.sessions?.firstOrNull()?.id}
 LaunchedEffect(visibleSnapshot!=null){if(visibleSnapshot!=null)while(true){delay(15_000);viewModel.refresh()}}
 LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{PageHeader("Hosted staff queue",onBack)}
  item{ElevatedSection("Stage 20B hosted clinic schedule",state.message){StatusPill(if(state.error)"Needs attention" else if(visibleSnapshot!=null)"Connected" else "Not connected",visibleSnapshot!=null&&!state.error);Text("Local Doctor/Assistant data remains separate and is never uploaded.",color=MaterialTheme.colorScheme.onSurfaceVariant)}}
  if(visibleSnapshot==null){item{ElevatedSection("Connect hosted identity",if(localRole==UserRole.DOCTOR)"Seeded Doctor" else "Seeded queue Assistant"){OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(4)},Modifier.fillMaxWidth(),label={Text("Demo PIN")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),visualTransformation=PasswordVisualTransformation(),singleLine=true);Text("Use demo PIN 1234. No real credential is sent.",color=MaterialTheme.colorScheme.onSurfaceVariant);PrimaryAction(if(state.loading)"Connecting..." else "Connect to hosted prototype",{viewModel.connect(if(localRole==UserRole.DOCTOR)HostedStaffRole.DOCTOR else HostedStaffRole.ASSISTANT,pin)},enabled=pin.length==4&&!state.loading)}}}
  visibleSnapshot?.let{snapshot->
   item{ElevatedSection(snapshot.clinic.name,"${snapshot.clinic.doctorName} • ${snapshot.clinic.city}"){Text("Identity: ${snapshot.role.name}",fontWeight=FontWeight.Bold);Text("Permissions: ${snapshot.permissions.sorted().joinToString()}",color=MaterialTheme.colorScheme.onSurfaceVariant);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({viewModel.refresh()},enabled=!state.loading){Text("Refresh")};OutlinedButton({viewModel.logout()}){Text("Disconnect hosted")}}}}
   if(snapshot.role==HostedStaffRole.DOCTOR){item{HostedClinicScheduleEditor(scheduleState,state.loading,viewModel::refreshClinicSchedule,viewModel::saveClinicSchedule,viewModel::saveScheduleException)};item{HostedDoctorProfileEditor(profileState,state.loading,viewModel::refreshDoctorProfile,viewModel::submitDoctorProfile)};item{HostedAnnouncementEditor(snapshot.clinic.id,snapshot.announcements,state.loading,viewModel::saveAnnouncement)};item{Text("Hosted Assistant access",style=MaterialTheme.typography.titleLarge)};if(snapshot.assistants.isEmpty())item{Text("No hosted Assistants assigned.",color=MaterialTheme.colorScheme.onSurfaceVariant)}else items(snapshot.assistants,key={"assistant-${it.id}"}){assistant->HostedAssistantAccessCard(assistant,state.loading,viewModel::updateAssistant)}}
   item{Text("Clinic sessions",style=MaterialTheme.typography.titleLarge)}
   items(snapshot.sessions,key={it.id}){session->FilterChip(selected=selectedSessionId==session.id,onClick={selectedSessionId=session.id},label={Text("${session.date} • ${session.name} • ${session.available} available")},modifier=Modifier.fillMaxWidth())}
   val session= snapshot.sessions.firstOrNull{it.id==selectedSessionId};val queue=snapshot.queues.firstOrNull{it.sessionId==selectedSessionId};val appointments=snapshot.appointments.filter{it.sessionId==selectedSessionId}
   if(session!=null){item{ElevatedSection("Authoritative queue","${session.date} • ${session.name}"){Text("Queue: ${queue?.status?:"NOT_STARTED"} | Current token: ${queue?.currentToken?:"None"}",fontWeight=FontWeight.Bold);val status=queue?.status?:"NOT_STARTED";val primary=when(status){"NOT_STARTED"->"START";"ACTIVE"->"PAUSE";"PAUSED"->"RESUME";else->null};if(primary!=null)PrimaryAction(primary.replace('_',' '),{viewModel.sessionCommand(session.id,status,queue?.lastCalledToken?:0,primary)},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions);PrimaryAction("Call next",{viewModel.sessionCommand(session.id,status,queue?.lastCalledToken?:0,"CALL_NEXT")},enabled=!state.loading&&status=="ACTIVE"&&"MANAGE_QUEUE" in snapshot.permissions)}}
    item{Text("Session appointments",style=MaterialTheme.typography.titleLarge)}
    if(appointments.isEmpty())item{Text("No hosted appointments in this session.",color=MaterialTheme.colorScheme.onSurfaceVariant)} else items(appointments,key={it.id}){appointment->val entry=queue?.entries?.firstOrNull{it.appointmentId==appointment.id};ElevatedSection("Token ${appointment.token} • ${appointment.patientName}","Status: ${entry?.status?:appointment.status}"){Text("Clinic fee: ${entry?.feeStatus?:appointment.feeStatus} | Receipt: ${entry?.receipt?.ifBlank{"Pending"}?:appointment.receipt.ifBlank{"Pending"}}",color=MaterialTheme.colorScheme.onSurfaceVariant);if(entry==null&&appointment.feeStatus=="PENDING")PrimaryAction("Confirm clinic fee and admit",{viewModel.admit(session.id,appointment.id)},enabled=!state.loading&&"CONFIRM_CLINIC_FEE" in snapshot.permissions);entry?.let{q->when(q.status){"IN_CONSULTATION"->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"COMPLETE")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Complete")};OutlinedButton({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"SKIP")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Skip")}};"SKIPPED"->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"RESUME_WAITING")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Resume waiting")};OutlinedButton({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"ABSENT")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Absent")}};else->Unit}}}}
   }
  }
 }
}
@Composable private fun HostedAssistantAccessCard(assistant:HostedAssistant,loading:Boolean,onSave:(HostedAssistant,Boolean,Set<String>)->Unit){
 var active by remember(assistant.id,assistant.active){mutableStateOf(assistant.active)}
 var queue by remember(assistant.id,assistant.permissions){mutableStateOf("MANAGE_QUEUE" in assistant.permissions)}
 var fee by remember(assistant.id,assistant.permissions){mutableStateOf("CONFIRM_CLINIC_FEE" in assistant.permissions)}
 ElevatedSection(assistant.displayName,assistant.phone){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Account active",fontWeight=FontWeight.Bold);Switch(active,{active=it})}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Manage queue");Switch(queue,{queue=it})}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Confirm clinic fee");Switch(fee,{fee=it})}
  Text("Changes are enforced by the hosted API. Disabling access blocks the Assistant's hosted session without changing local app data.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  PrimaryAction("Save hosted access",{onSave(assistant,active,buildSet{if(queue)add("MANAGE_QUEUE");if(fee)add("CONFIRM_CLINIC_FEE")})},enabled=!loading)
 }
}

@Composable private fun HostedAnnouncementEditor(clinicId:String,announcements:List<HostedAnnouncement>,loading:Boolean,onSave:(HostedAnnouncement)->Unit){
 var editing by remember{mutableStateOf<HostedAnnouncement?>(null)}
 val today=remember{LocalDate.now().toString()}
 var kind by remember(editing){mutableStateOf(editing?.kind?:"DOCTOR_GENERAL")}
 var title by remember(editing){mutableStateOf(editing?.title.orEmpty())}
 var message by remember(editing){mutableStateOf(editing?.message.orEmpty())}
 var startsOn by remember(editing){mutableStateOf(editing?.startsOn?:today)}
 var endsOn by remember(editing){mutableStateOf(editing?.endsOn?:today)}
 var active by remember(editing){mutableStateOf(editing?.active?:true)}
 ElevatedSection("Hosted Patient announcements","Visible on the Patient App only while active and within the selected dates"){
  Text("Announcement type",fontWeight=FontWeight.Bold)
  listOf("DOCTOR_AVAILABILITY" to "Availability","DOCTOR_CAMP" to "Camp","DOCTOR_OFFER" to "Offer","DOCTOR_GENERAL" to "General").chunked(2).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){row.forEach{(value,label)->FilterChip(selected=kind==value,onClick={kind=value},label={Text(label)},modifier=Modifier.weight(1f))}}}
  OutlinedTextField(title,{title=it.take(100)},Modifier.fillMaxWidth(),label={Text("Title")},singleLine=true)
  OutlinedTextField(message,{message=it.take(500)},Modifier.fillMaxWidth(),label={Text("Message")},minLines=3)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(startsOn,{startsOn=it.take(10)},Modifier.weight(1f),label={Text("Starts YYYY-MM-DD")},singleLine=true);OutlinedTextField(endsOn,{endsOn=it.take(10)},Modifier.weight(1f),label={Text("Ends YYYY-MM-DD")},singleLine=true)}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Published and active");Switch(active,{active=it})}
  PrimaryAction(if(editing==null)"Save hosted announcement" else "Update hosted announcement",{onSave(HostedAnnouncement(editing?.id.orEmpty(),clinicId,kind,title.trim(),message.trim(),startsOn,endsOn,active))},enabled=!loading&&title.isNotBlank()&&message.isNotBlank()&&startsOn.length==10&&endsOn.length==10)
  if(editing!=null)OutlinedButton({editing=null},Modifier.fillMaxWidth()){Text("Cancel editing")}
  if(announcements.isEmpty())Text("No hosted announcements have been created.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  announcements.forEach{announcement->HorizontalDivider();Text(announcement.title,fontWeight=FontWeight.Bold);Text("${announcement.kind.replace('_',' ')} | ${announcement.startsOn} to ${announcement.endsOn} | ${if(announcement.active)"Active" else "Draft"}",color=MaterialTheme.colorScheme.onSurfaceVariant);Text(announcement.message);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({editing=announcement},enabled=!loading){Text("Edit")};OutlinedButton({onSave(announcement.copy(active=!announcement.active))},enabled=!loading){Text(if(announcement.active)"Set draft" else "Publish")}}}
 }
}
@Composable private fun HostedDoctorProfileEditor(state:HostedDoctorProfileUiState,queueLoading:Boolean,onRefresh:()->Unit,onSubmit:(HostedDoctorProfile)->Unit){
 val workspace=state.workspace
 if(workspace==null){ElevatedSection("Reviewed Doctor profile",state.message){StatusPill(if(state.error)"Needs attention" else "Loading",false);PrimaryAction("Load approved profile",onRefresh,enabled=!state.loading&&!queueLoading)};return}
 val approved=workspace.profile;val draft=workspace.pendingRevision
 var displayName by remember(approved.profileRevision,draft?.id){mutableStateOf(draft?.displayName?:approved.displayName)}
 var registration by remember(approved.profileRevision,draft?.id){mutableStateOf(draft?.registrationNumber?:approved.registrationNumber)}
 var specialty by remember(approved.profileRevision,draft?.id){mutableStateOf(draft?.specialty?:approved.specialty)}
 var qualification by remember(approved.profileRevision,draft?.id){mutableStateOf(draft?.qualification?:approved.qualification)}
 var experience by remember(approved.profileRevision,draft?.id){mutableStateOf((draft?.experienceYears?:approved.experienceYears).toString())}
 var about by remember(approved.profileRevision,draft?.id){mutableStateOf(draft?.about?:approved.about)}
 val years=experience.toIntOrNull();val registrationValid=registration.trim().matches(Regex("[A-Za-z0-9][A-Za-z0-9 ./-]{1,79}"));val valid=displayName.trim().length in 2..100&&registrationValid&&specialty.trim().length in 2..80&&qualification.trim().length in 2..160&&years!=null&&years in 0..80&&about.trim().length<=1000
 ElevatedSection("Reviewed Doctor profile","Approved revision ${approved.profileRevision} | ${approved.verificationStatus}"){
  Text("Patient-facing values change only after Admin approval.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  draft?.let{StatusPill("Pending Admin review",true);Text("Submitted ${it.submittedAt}",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  Text("Approved: ${approved.displayName} | ${approved.specialty}",fontWeight=FontWeight.Bold)
  OutlinedTextField(displayName,{displayName=it.take(100)},Modifier.fillMaxWidth(),label={Text("Doctor display name")},singleLine=true)
  OutlinedTextField(registration,{registration=it.take(80)},Modifier.fillMaxWidth(),label={Text("Registration number")},singleLine=true,isError=registration.isNotBlank()&&!registrationValid)
  OutlinedTextField(specialty,{specialty=it.take(80)},Modifier.fillMaxWidth(),label={Text("Specialty")},singleLine=true)
  OutlinedTextField(qualification,{qualification=it.take(160)},Modifier.fillMaxWidth(),label={Text("Qualification")},singleLine=true)
  OutlinedTextField(experience,{experience=it.filter(Char::isDigit).take(2)},Modifier.fillMaxWidth(),label={Text("Experience years")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true)
  OutlinedTextField(about,{about=it.take(1000)},Modifier.fillMaxWidth(),label={Text("About")},minLines=3,supportingText={Text("${about.length}/1000")})
  Text(state.message,color=if(state.error)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
  PrimaryAction(if(draft==null)"Submit profile for review" else "Replace pending revision",{onSubmit(approved.copy(displayName=displayName.trim(),registrationNumber=registration.trim(),specialty=specialty.trim(),qualification=qualification.trim(),experienceYears=years?:0,about=about.trim()))},enabled=valid&&!state.loading&&!queueLoading)
  OutlinedButton(onRefresh,Modifier.fillMaxWidth(),enabled=!state.loading&&!queueLoading){Text("Refresh review status")}
 }
}
